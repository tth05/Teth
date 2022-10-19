package com.github.tth05.teth.analyzer.completion;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.analyzer.type.SemanticType;
import com.github.tth05.teth.lang.parser.ASTUtil;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;
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
            collectDeclarationsFromCollection("", results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
            collectFromUseStatements("", results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
            return results;
        }

        var first = stack.pop();
        if (first instanceof IdentifierExpression expr) {
            var text = expr.getValue() == null ? "" : expr.getValue().substring(0, offset - expr.getSpan().offset());
            if (stack.peek() instanceof TypeExpression || stack.peek() instanceof ObjectCreationExpression) { // Types
                collectPreludeStructs(text, results);
                collectDeclarationsFromCollection(text, results, stack, CompletionItem.SCOPE_STACK_PRIORITY, (s) -> s instanceof StructDeclaration);
                collectDeclarationsFromCollection(text, results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof StructDeclaration);
                collectFromUseStatements(text, results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof StructDeclaration);
            } else if (stack.peek() instanceof MemberAccessExpression memberAccess) { // Members
                collectMemberAccess(text, results, memberAccess);
            } else { // Just an identifier
                collectIdentifier(text, results, stack);
            }
        } else if (first instanceof MemberAccessExpression memberAccess) {
            collectMemberAccess("", results, memberAccess);
        } else if (first instanceof BlockStatement) {
            collectIdentifier("", results, stack);
        }

        return results;
    }

    private void collectIdentifier(String text, List<CompletionItem> results, Deque<Statement> stack) {
        collectPreludeFunctions(text, results);
        collectDeclarationsFromCollection(text, results, stack, CompletionItem.SCOPE_STACK_PRIORITY, (s) -> s instanceof FunctionDeclaration f && !f.isInstanceFunction());
        collectDeclarationsFromCollection(text, results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
        collectFromUseStatements(text, results, this.unit.getStatements(), CompletionItem.TOP_LEVEL_PRIORITY, (s) -> s instanceof FunctionDeclaration);
    }

    private void collectMemberAccess(String text, List<CompletionItem> results, MemberAccessExpression memberAccess) {
        var type = this.analyzer.resolvedExpressionType(memberAccess.getTarget());
        if (type == SemanticType.UNRESOLVED)
            return;

        if (!(this.analyzer.getTypeCache().getDeclaration(type) instanceof StructDeclaration structDeclaration))
            return;

        collectDeclarationsFromCollection(text, results, structDeclaration.getFields(), CompletionItem.LOCALS_PRIORITY, (s) -> true);
        collectDeclarationsFromCollection(text, results, structDeclaration.getFunctions(), CompletionItem.LOCALS_PRIORITY, (s) -> true);
    }

    private void collectFromUseStatements(String text, List<CompletionItem> results, List<Statement> allStatements, int priority, Predicate<Statement> predicate) {
        for (var statement : allStatements) {
            if (!(statement instanceof UseStatement useStatement))
                continue;

            for (var importExpr : useStatement.getImports()) {
                var ref = this.analyzer.resolvedReference(importExpr);
                if (ref == null || !predicate.test(ref))
                    continue;

                addNamed(text, results, ref, priority);
            }
        }
    }

    private static <T extends Statement> void collectDeclarationsFromCollection(String text,
                                                                                List<CompletionItem> results,
                                                                                Collection<T> stack,
                                                                                int priority,
                                                                                Predicate<T> predicate) {
        for (var statement : stack) {
            if (!predicate.test(statement))
                continue;

            addNamed(text, results, statement, priority);
        }
    }

    private static void collectPreludeFunctions(String text, List<CompletionItem> results) {
        for (var f : Prelude.getGlobalFunctions())
            addFunction(text, results, f, CompletionItem.PRELUDE_PRIORITY);
    }

    private static void collectPreludeStructs(String text, List<CompletionItem> results) {
        for (var s : Prelude.getGlobalStructs())
            addStruct(text, results, s, CompletionItem.PRELUDE_PRIORITY);
    }

    private static void addFunction(String text, List<CompletionItem> results, FunctionDeclaration function, int priority) {
        addNamed(text, results, function, priority);
    }

    private static void addStruct(String text, List<CompletionItem> results, StructDeclaration struct, int priority) {
        addNamed(text, results, struct, priority);
    }

    private static void addNamed(String text, List<CompletionItem> results, Statement statement, int priority) {
        if (!(statement instanceof IHasName named))
            return;

        var name = named.getNameExpr().getValue();
        if (name == null || !name.startsWith(text))
            return;

        results.add(new CompletionItem(name, getTailText(statement), getTypeText(statement), getType(statement), priority));
    }

    private static CompletionItem.Type getType(Statement statement) {
        if (statement instanceof FunctionDeclaration)
            return CompletionItem.Type.FUNCTION;
        else if (statement instanceof StructDeclaration)
            return CompletionItem.Type.STRUCT;
        else if (statement instanceof StructDeclaration.FieldDeclaration)
            return CompletionItem.Type.FIELD;
        else if (statement instanceof VariableDeclaration || statement instanceof FunctionDeclaration.ParameterDeclaration)
            return CompletionItem.Type.VARIABLE;
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
