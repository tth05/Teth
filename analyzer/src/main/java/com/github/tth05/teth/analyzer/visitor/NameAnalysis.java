package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.ScopeStack;
import com.github.tth05.teth.analyzer.module.ModuleCache;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.BiIterator;

import java.util.*;
import java.util.stream.Collectors;

public class NameAnalysis extends AnalysisASTVisitor {

    public static final FunctionDeclaration GLOBAL_FUNCTION = new FunctionDeclaration(null, null, new IdentifierExpression(null, null), List.of(), List.of(), null, null, false);

    private static final HashSet<String> DUPLICATION_SET = new HashSet<>();

    private final ScopeStack scopeStack = new ScopeStack();

    private final Analyzer analyzer;
    private final Map<IDeclarationReference, Statement> resolvedReferences;
    private final Map<FunctionDeclaration, Integer> functionLocalsCount;

    private SourceFileUnit unit;

    public NameAnalysis(Analyzer analyzer, Map<IDeclarationReference, Statement> resolvedReferences, Map<FunctionDeclaration, Integer> functionLocalsCount) {
        this.analyzer = analyzer;
        this.resolvedReferences = resolvedReferences;
        this.functionLocalsCount = functionLocalsCount;
    }

    public void preDeclVisit(SourceFileUnit unit) {
        if (this.unit != null)
            throw new IllegalStateException("preDeclVisit has already been called");
        this.unit = unit;

        beginFunctionDeclaration(GLOBAL_FUNCTION);

        List<? extends Statement> topLevelDeclarations = unit.getStatements().stream()
                .filter(s -> s instanceof ITopLevelDeclaration && s instanceof IHasName || s instanceof UseStatement)
                .sorted(new TopLevelDeclComparator())
                .collect(Collectors.toCollection(() -> new ArrayList<>(unit.getStatements().size())));

        // Pre-process 1: Collect all declarations
        for (var decl : topLevelDeclarations) {
            if (decl instanceof UseStatement)
                continue;

            addDeclaration(decl);
        }

        // Pre-process 2: Analyze headers of top level functions and structs and check for duplicates
        {
            DUPLICATION_SET.clear();

            for (var decl : topLevelDeclarations) {
                if (decl instanceof UseStatement)
                    continue;

                var name = ((IHasName) decl).getNameExpr();
                if (!DUPLICATION_SET.add(name.getValue()))
                    report(name.getSpan(), "Duplicate top level declaration");
            }
        }

        for (var decl : topLevelDeclarations) {
            if (decl instanceof StructDeclaration structDeclaration) {
                this.scopeStack.beginScope(structDeclaration);
                structDeclaration.getGenericParameters().forEach(this::addDeclaration);
                structDeclaration.getFields().forEach(f -> visit(f.getTypeExpr()));
                structDeclaration.getFunctions().forEach(this::visitFunctionDeclarationHeader);
                this.scopeStack.endScope();
            } else if (decl instanceof FunctionDeclaration functionDeclaration) {
                this.scopeStack.beginScope(functionDeclaration);
                visitFunctionDeclarationHeader(functionDeclaration);
                this.scopeStack.endScope();
            } else if (decl instanceof UseStatement) {
                decl.accept(this);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public void visit(SourceFileUnit unit) {
        if (this.unit == null)
            throw new IllegalStateException("Pre declaration visit was not called");

        super.visit(unit);
    }

    @Override
    public void visit(UseStatement useStatement) {
        var pathExpr = useStatement.getPathExpr();
        var path = pathExpr != null ? pathExpr.asSingleString() : null;

        if (!ModuleCache.isValidModulePath(path)) {
            var span = pathExpr != null ? pathExpr.getSpan() : useStatement.getSpan();
            report(span, "Invalid module path");
            return;
        }

        var uniquePath = this.analyzer.toUniquePath(this.unit.getUniquePath(), path);
        if (uniquePath.equals(this.unit.getUniquePath())) {
            report(pathExpr.getSpan(), "Cannot use 'this' module path");
            return;
        }

        if (!this.analyzer.hasModule(uniquePath)) {
            report(pathExpr.getSpan(), "Module '" + path + "' does not exist");
            return;
        }

        for (var importNameExpr : useStatement.getImports()) {
            var decl = this.analyzer.findExportedDeclaration(uniquePath, importNameExpr.getValue());
            if (decl == null) {
                report(importNameExpr.getSpan(), "Type or function '" + importNameExpr.getValue() + "' not found in module '" + path + "'");
                continue;
            }

            addDeclaration(decl);
            this.resolvedReferences.put(importNameExpr, decl);
        }
    }

    @Override
    public void visit(FunctionDeclaration declaration) {
        if (declaration.isIntrinsic())
            return;

        // Add nested functions to outer scope, as if this were a variable declaration
        if (isInFunction() && !declaration.isInstanceFunction())
            addDeclaration(declaration);

        var enclosingStruct = declaration.isInstanceFunction() ? this.scopeStack.getCurrentOfType(StructDeclaration.class) : null;
        beginFunctionDeclaration(declaration);
        if (isInNestedFunction())
            visitFunctionDeclarationHeader(declaration);
        visitFunctionDeclarationBody(declaration, enclosingStruct);

        this.scopeStack.endScope();
    }

    @Override
    public void visit(FunctionDeclaration.ParameterDeclaration parameter) {
        validateNotAReservedName(parameter.getNameExpr());

        { // Don't want to visit parameter name here
            parameter.getTypeExpr().accept(this);
        }
    }

    @Override
    public void visit(FunctionInvocationExpression invocation) {
        super.visit(invocation);

        //TODO: Move to parser
        if (!(invocation.getTarget() instanceof IDeclarationReference))
            report(invocation.getTarget().getSpan(), "Function invocation target must be a function");
    }

    @Override
    public void visit(StructDeclaration declaration) {
        if (!declaration.isIntrinsic())
            validateNotAReservedName(declaration.getNameExpr());

        validateNoDuplicates(declaration.getGenericParameters(), "Duplicate generic parameter name");
        validateNoDuplicates(BiIterator.of(declaration.getFields(), declaration.getFunctions()), "Duplicate member name");

        if (isInFunction())
            addDeclaration(declaration);

        this.scopeStack.beginScope(declaration);
        { // Don't want to visit struct name here
            declaration.getFields().forEach(p -> p.accept(this));
            declaration.getFunctions().forEach(p -> p.accept(this));
        }
        this.scopeStack.endScope();
    }

    @Override
    public void visit(StructDeclaration.FieldDeclaration declaration) {
        validateNotAReservedName(declaration.getNameExpr());

        // Pre-process 2 did this already in the global namespace
        if (isInNestedFunction())
            declaration.getTypeExpr().accept(this);
    }

    @Override
    public void visit(ObjectCreationExpression expression) {
        super.visit(expression);

        var struct = this.resolvedReferences.get(expression.getTargetNameExpr());
        if (struct == null)
            return;
        if (!(struct instanceof StructDeclaration)) {
            report(expression.getTargetNameExpr().getSpan(), "Object creation target must be a struct");
            return;
        }

        this.resolvedReferences.put(expression, struct);
    }

    @Override
    public void visit(MemberAccessExpression expression) {
        { // Don't want to visit member name here
            expression.getTarget().accept(this);
        }

        // Impossible to-do anything here without type info :(
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        super.visit(returnStatement);

        if (getFunctionStackSize() == 1)
            report(returnStatement.getSpan(), "Return statement outside of function");
    }

    @Override
    public void visit(VariableDeclaration declaration) {
        validateNotAReservedName(declaration.getNameExpr());

        { // Don't want to visit name here
            var typeExpr = declaration.getTypeExpr();
            if (typeExpr != null)
                typeExpr.accept(this);
            declaration.getInitializerExpr().accept(this);
        }

        this.functionLocalsCount.merge(this.scopeStack.getClosestOfType(FunctionDeclaration.class), 1, Integer::sum);

        addDeclaration(declaration);
    }

    @Override
    public void visit(LoopStatement statement) {
        this.scopeStack.beginSubScope(statement);
        super.visit(statement);
        this.scopeStack.endScope();
    }

    @Override
    public void visit(BreakStatement statement) {
        var loopStatement = this.scopeStack.getClosestOfType(LoopStatement.class);
        if (loopStatement == null)
            report(statement.getSpan(), "Break statement outside of loop");

        this.resolvedReferences.put(statement, loopStatement);
    }

    @Override
    public void visit(ContinueStatement statement) {
        var loopStatement = this.scopeStack.getClosestOfType(LoopStatement.class);
        if (loopStatement == null)
            report(statement.getSpan(), "Continue statement outside of loop");

        this.resolvedReferences.put(statement, loopStatement);
    }

    @Override
    public void visit(BlockStatement statement) {
        this.scopeStack.beginSubScope(statement);
        super.visit(statement);
        this.scopeStack.endScope();
    }

    @Override
    public void visit(GenericParameterDeclaration declaration) {
        validateNotAReservedName(declaration.getNameExpr());

        addDeclaration(declaration);
    }

    @Override
    public void visit(TypeExpression typeExpression) {
        var span = typeExpression.getSpan();
        var type = typeExpression.getNameExpr().getValue();
        var genericParameters = typeExpression.getGenericParameters();
        genericParameters.forEach(t -> t.accept(this));

        if (type == null)
            return;

        Statement decl;
        if (Prelude.isBuiltInTypeName(type))
            decl = Prelude.getStructForTypeName(type);
        else
            decl = this.scopeStack.resolveIdentifier(type);

        if (decl == null) {
            report(span, "Unknown type " + type);
            return;
        }
        if (!(decl instanceof StructDeclaration) && !(decl instanceof GenericParameterDeclaration)) {
            report(span, "Type " + type + " is not a struct or builtin type");
            return;
        }

        // Ensure all generic parameters are bound
        if (decl instanceof StructDeclaration struct) {
            var genericParameterDeclarations = struct.getGenericParameters();
            if (genericParameterDeclarations.size() != genericParameters.size())
                report(Span.of(genericParameters, span), "Wrong number of generic parameters. Expected %d, got %d".formatted(genericParameterDeclarations.size(), genericParameters.size()));
        }
        if (decl instanceof GenericParameterDeclaration && !genericParameters.isEmpty())
            report(span, "Generic parameter cannot have generic parameters");

        this.resolvedReferences.put(typeExpression.getNameExpr(), decl);
        this.resolvedReferences.put(typeExpression, decl);
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        var decl = this.scopeStack.resolveIdentifier(identifierExpression.getValue());
        if (decl == null)
            decl = Prelude.getGlobalFunction(identifierExpression.getValue());
        if (decl == null) {
            report(identifierExpression.getSpan(), "Unresolved identifier");
            return;
        }

        if (!(decl instanceof VariableDeclaration) &&
            !(decl instanceof FunctionDeclaration) &&
            !(decl instanceof FunctionDeclaration.ParameterDeclaration) &&
            !(decl instanceof StructDeclaration))
            report(identifierExpression.getSpan(), "Identifier is not a variable, function or struct");

        this.resolvedReferences.put(identifierExpression, decl);
    }

    private void visitFunctionDeclarationHeader(FunctionDeclaration declaration) {
        if (!declaration.isIntrinsic())
            validateNotAReservedName(declaration.getNameExpr());

        validateNoDuplicates(declaration.getGenericParameters(), "Duplicate generic parameter name");
        declaration.getGenericParameters().forEach(p -> p.accept(this));

        validateNoDuplicates(declaration.getParameters(), "Duplicate parameter name");
        declaration.getParameters().forEach(p -> p.accept(this));

        var returnTypeExpr = declaration.getReturnTypeExpr();
        if (returnTypeExpr != null)
            returnTypeExpr.accept(this);
    }

    private void visitFunctionDeclarationBody(FunctionDeclaration declaration, StructDeclaration enclosingStruct) {
        // Parameters
        declaration.getGenericParameters().forEach(this::addDeclaration);
        declaration.getParameters().forEach(this::addDeclaration);

        if (declaration.isInstanceFunction()) {
            var selfParameter = new FunctionDeclaration.ParameterDeclaration(
                    null,
                    new TypeExpression(null,
                            enclosingStruct.getNameExpr(),
                            enclosingStruct.getGenericParameters().stream()
                                    .map(p -> {
                                        var typeExpr = new TypeExpression(null, new IdentifierExpression(null, p.getNameExpr().getValue()));
                                        this.resolvedReferences.put(typeExpr, p);
                                        return typeExpr;
                                    })
                                    .collect(Collectors.toList())
                    ),
                    new IdentifierExpression(null, "self")
            );
            addDeclaration(selfParameter);
            this.resolvedReferences.put(selfParameter.getTypeExpr(), enclosingStruct);
        }

        declaration.getBody().accept(this);
    }

    private void validateNoDuplicates(List<? extends IHasName> list, String message) {
        validateNoDuplicates(list.iterator(), message);
    }

    private void validateNoDuplicates(Iterator<? extends IHasName> it, String message) {
        DUPLICATION_SET.clear();

        while (it.hasNext()) {
            var el = it.next();
            if (!DUPLICATION_SET.add(el.getNameExpr().getValue()))
                report(el.getNameExpr().getSpan(), message);
        }
    }

    private void validateNotAReservedName(IdentifierExpression expression) {
        var name = expression.getValue();
        if (Prelude.isBuiltInTypeName(name))
            report(expression.getSpan(), "Reserved name '" + name + "'");
    }

    private void addDeclaration(Statement declaration) {
        if (!(declaration instanceof IHasName named))
            throw new IllegalArgumentException(declaration + "");

        var name = named.getNameExpr().getValue();
        if (name == null)
            return;

        this.scopeStack.addDeclaration(name, declaration);
    }

    private void beginFunctionDeclaration(FunctionDeclaration declaration) {
        this.scopeStack.beginScope(declaration);
        this.functionLocalsCount.put(declaration, 0);
    }

    private boolean isInFunction() {
        return getFunctionStackSize() > 1;
    }

    private boolean isInNestedFunction() {
        return getFunctionStackSize() > 2;
    }

    private int getFunctionStackSize() {
        return this.scopeStack.countScopesOfType(FunctionDeclaration.class);
    }

    private static class TopLevelDeclComparator implements Comparator<Statement> {

        @Override
        public int compare(Statement a, Statement b) {
            var aIsUse = a instanceof UseStatement;
            var bIsUse = b instanceof UseStatement;
            if (aIsUse && bIsUse)
                return 0;
            if (!aIsUse && !bIsUse)
                return Integer.compare(getPriority(a), getPriority(b));
            if (a.getSpan() == null)
                return -1;
            if (b.getSpan() == null)
                return 1;
            return Integer.compare(a.getSpan().offset(), b.getSpan().offset());
        }

        private int getPriority(Statement statement) {
            if (statement instanceof StructDeclaration)
                return 0;
            if (statement instanceof FunctionDeclaration)
                return 1;
            return 2;
        }
    }
}
