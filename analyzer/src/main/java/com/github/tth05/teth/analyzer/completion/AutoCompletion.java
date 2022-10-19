package com.github.tth05.teth.analyzer.completion;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.type.SemanticType;
import com.github.tth05.teth.lang.parser.ASTUtil;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.*;
import java.util.function.Predicate;

public class AutoCompletion {

    private final Analyzer analyzer;
    private final SourceFileUnit unit;

    public AutoCompletion(Analyzer analyzer, SourceFileUnit unit) {
        this.analyzer = analyzer;
        this.unit = unit;
    }

    public List<CompletionItem> complete(int offset) {
        var stack = new ArrayDeque<Statement>();
        ASTUtil.walkNodesAtOffset(this.unit.getStatements(), offset, true, stack::push);

        var results = new ArrayList<CompletionItem>();
        if (stack.isEmpty()) {
            collectDeclarationsFromCollection(results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
            collectFromUseStatements(results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
            collectLocals(results, new LinkedList<>(), offset);
            return results;
        }

        var first = stack.peek();
        if (first instanceof IdentifierExpression expr) {
            // We keep the first element in the stack to allow collectLocals to work correctly
            stack.pop();
            var second = stack.peek();
            stack.push(first);

            var text = expr.getValue() == null ? "" : expr.getValue().substring(0, offset - expr.getSpan().offset());
            if (second instanceof TypeExpression ||
                (second instanceof ObjectCreationExpression objExpr && offset <= Optional.ofNullable(objExpr.getTargetNameExpr().getSpan()).map(Span::offsetEnd).orElse(offset))) { // Types
                collectTypes(results, stack);
            } else if (second instanceof MemberAccessExpression memberAccess) { // Members
                collectMemberAccess(results, memberAccess);
            } else { // Just an identifier
                collectIdentifier(results, stack, offset);
            }
        } else if (first instanceof MemberAccessExpression memberAccess) {
            collectMemberAccess(results, memberAccess);
        } else if (first instanceof BlockStatement) {
            collectIdentifier(results, stack, offset);
        } else if (first instanceof TypeExpression) {
            collectTypes(results, stack);
        }

        return results;
    }

    private void collectTypes(ArrayList<CompletionItem> results, ArrayDeque<Statement> stack) {
        collectPreludeStructs(results);
        collectDeclarationsFromCollection(results, stack, CompletionItem.SCOPE_STACK_PRIORITY, (s) -> s instanceof StructDeclaration);
        collectDeclarationsFromCollection(results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof StructDeclaration);
        collectFromUseStatements(results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof StructDeclaration);
    }

    private void collectIdentifier(List<CompletionItem> results, Deque<Statement> stack, int offset) {
        collectPreludeFunctions(results);
        collectDeclarationsFromCollection(results, stack, CompletionItem.SCOPE_STACK_PRIORITY, (s) -> s instanceof FunctionDeclaration f && !f.isInstanceFunction());
        collectDeclarationsFromCollection(results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
        collectFromUseStatements(results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
        collectLocals(results, stack, offset);
    }

    private void collectMemberAccess(List<CompletionItem> results, MemberAccessExpression memberAccess) {
        var type = this.analyzer.resolvedExpressionType(memberAccess.getTarget());
        if (type == SemanticType.UNRESOLVED)
            return;

        if (!(this.analyzer.getTypeCache().getDeclaration(type) instanceof StructDeclaration structDeclaration))
            return;

        collectDeclarationsFromCollection(results, structDeclaration.getFields(), CompletionItem.LOCALS_PRIORITY, (s) -> true);
        collectDeclarationsFromCollection(results, structDeclaration.getFunctions(), CompletionItem.LOCALS_PRIORITY, (s) -> true);
    }

    private void collectLocals(List<CompletionItem> results, Deque<Statement> stack, int offset) {
        for (var statement : stack) {
            if (statement instanceof FunctionDeclaration function) {
                function.getParameters().forEach(p -> addNamed(results, p, CompletionItem.LOCALS_PRIORITY));
                return;
            } else if (statement instanceof LoopStatement loop) {
                loop.getVariableDeclarations().forEach(v -> addNamed(results, v, CompletionItem.LOCALS_PRIORITY));
            } else if (statement instanceof BlockStatement block) {
                block.getStatements().forEach(s -> {
                    if (s instanceof VariableDeclaration variable && variable.getSpan().offset() < offset)
                        addNamed(results, variable, CompletionItem.LOCALS_PRIORITY);
                });
            }
        }

        for (var statement : this.unit.getStatements()) {
            if (!(statement instanceof VariableDeclaration) || statement.getSpan().offset() > offset)
                continue;

            addNamed(results, statement, CompletionItem.LOCALS_PRIORITY);
        }
    }

    private void collectFromUseStatements(List<CompletionItem> results, List<Statement> allStatements, int priority, Predicate<Statement> predicate) {
        for (var statement : allStatements) {
            if (!(statement instanceof UseStatement useStatement))
                continue;

            for (var importExpr : useStatement.getImports()) {
                var ref = this.analyzer.resolvedReference(importExpr);
                if (ref == null || !predicate.test(ref))
                    continue;

                addNamed(results, ref, priority);
            }
        }
    }

    private static <T extends Statement> void collectDeclarationsFromCollection(List<CompletionItem> results,
                                                                                Collection<T> stack,
                                                                                int priority,
                                                                                Predicate<T> predicate) {
        for (var statement : stack) {
            if (!predicate.test(statement))
                continue;

            addNamed(results, statement, priority);
        }
    }

    private static void collectPreludeFunctions(List<CompletionItem> results) {
        for (var f : Prelude.getGlobalFunctions())
            addFunction(results, f, CompletionItem.PRELUDE_PRIORITY);
    }

    private static void collectPreludeStructs(List<CompletionItem> results) {
        for (var s : Prelude.getGlobalStructs())
            addStruct(results, s, CompletionItem.PRELUDE_PRIORITY);
    }

    private static void addFunction(List<CompletionItem> results, FunctionDeclaration function, int priority) {
        addNamed(results, function, priority);
    }

    private static void addStruct(List<CompletionItem> results, StructDeclaration struct, int priority) {
        addNamed(results, struct, priority);
    }

    private static void addNamed(List<CompletionItem> results, Statement statement, int priority) {
        if (!(statement instanceof IHasName named))
            return;

        var name = named.getNameExpr().getValue();
        if (name == null)
            return;

        for (int i = results.size() - 1; i >= 0; i--) {
            var item = results.get(i);
            if (!item.text().equals(name))
                continue;

            if (item.priority() <= priority)
                results.set(i, new CompletionItem(name, getTailText(statement), getTypeText(statement), getType(statement), priority));

            return;
        }

        results.add(new CompletionItem(name, getTailText(statement), getTypeText(statement), getType(statement), priority));
    }

    private static CompletionItem.Type getType(Statement statement) {
        // TODO: Switch preview disabled
        //noinspection IfCanBeSwitch
        if (statement instanceof FunctionDeclaration)
            return CompletionItem.Type.FUNCTION;
        else if (statement instanceof StructDeclaration)
            return CompletionItem.Type.STRUCT;
        else if (statement instanceof StructDeclaration.FieldDeclaration)
            return CompletionItem.Type.FIELD;
        else if (statement instanceof VariableDeclaration)
            return CompletionItem.Type.VARIABLE;
        else if (statement instanceof FunctionDeclaration.ParameterDeclaration)
            return CompletionItem.Type.PARAMETER;
        else
            throw new IllegalArgumentException("Unknown statement type: " + statement.getClass().getName());
    }

    private static String getTailText(Statement statement) {
        if (statement instanceof FunctionDeclaration func) {
            var builder = new ASTDumpBuilder();
            builder.append("(");
            for (int i = 0; i < func.getParameters().size(); i++) {
                var parameter = func.getParameters().get(i);
                builder.append(parameter.getNameExpr().getValue());
                builder.append(": ");
                parameter.getTypeExpr().dump(builder);
                if (i != func.getParameters().size() - 1)
                    builder.append(", ");
            }
            builder.append(")");
            return builder.toString();
        } else if (statement instanceof StructDeclaration struct && !struct.getGenericParameters().isEmpty()) {
            var builder = new StringBuilder();
            builder.append("<");
            for (int i = 0; i < struct.getGenericParameters().size(); i++) {
                var genericParameter = struct.getGenericParameters().get(i);
                builder.append(genericParameter.getNameExpr().getValue());
                if (i != struct.getGenericParameters().size() - 1)
                    builder.append(", ");
            }
            builder.append(">");
            return builder.toString();
        }
        return null;
    }

    private static String getTypeText(Statement statement) {
        if (statement instanceof FunctionDeclaration func && func.getReturnTypeExpr() != null) {
            var builder = new ASTDumpBuilder();
            func.getReturnTypeExpr().dump(builder);
            return builder.toString();
        } else if (statement instanceof StructDeclaration.FieldDeclaration field) {
            var builder = new ASTDumpBuilder();
            field.getTypeExpr().dump(builder);
            return builder.toString();
        } else if (statement instanceof FunctionDeclaration.ParameterDeclaration param) {
            var builder = new ASTDumpBuilder();
            param.getTypeExpr().dump(builder);
            return builder.toString();
        } else if (statement instanceof VariableDeclaration var && var.getTypeExpr() != null) {
            // TODO: Use resolved type when no explicit type is available
            var builder = new ASTDumpBuilder();
            var.getTypeExpr().dump(builder);
            return builder.toString();
        }
        return null;
    }
}
