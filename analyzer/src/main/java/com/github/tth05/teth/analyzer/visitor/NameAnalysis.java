package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.analyzer.DeclarationStack;
import com.github.tth05.teth.analyzer.ValidationException;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;

import java.util.*;
import java.util.stream.Collectors;

public class NameAnalysis extends ASTVisitor {

    public static final FunctionDeclaration GLOBAL_FUNCTION = new FunctionDeclaration(null, null, List.of(), List.of(), null, null, false);

    private final Deque<FunctionDeclaration> currentFunctionStack = new ArrayDeque<>(5);
    private final DeclarationStack declarationStack = new DeclarationStack();

    private final Map<IDeclarationReference, Statement> resolvedReferences;
    private final Map<FunctionDeclaration, Integer> functionLocalsCount;

    public NameAnalysis(Map<IDeclarationReference, Statement> resolvedReferences, Map<FunctionDeclaration, Integer> functionLocalsCount) {
        this.resolvedReferences = resolvedReferences;
        this.functionLocalsCount = functionLocalsCount;
    }

    @Override
    public void visit(SourceFileUnit unit) {
        beginFunctionDeclaration(GLOBAL_FUNCTION);

        var statements = new ArrayList<>(unit.getStatements());
        // Note: This re-orders other statements as well, but that's fine because the sort is stable.
        statements.sort(Comparator.comparingInt(s -> switch (s) {
            case StructDeclaration sd -> 0;
            case FunctionDeclaration fd -> 1;
            default -> 2;
        }));
        // Pre-process 1: Collect all declarations
        statements.stream()
                .filter(s -> s instanceof ITopLevelDeclaration)
                .forEach(this::addDeclaration);

        // Pre-process 2: Analyze headers of top level functions and structs
        statements.stream()
                .filter(s -> s instanceof ITopLevelDeclaration)
                .forEach(decl -> {
                    switch (decl) {
                        case StructDeclaration structDeclaration -> {
                            this.declarationStack.beginScope(false);
                            structDeclaration.getGenericParameters().forEach(this::addDeclaration);
                            structDeclaration.getFields().forEach(f -> visit(f.getTypeExpr()));
                            structDeclaration.getFunctions().forEach(this::visitFunctionDeclarationHeader);
                            this.declarationStack.endScope();
                        }
                        case FunctionDeclaration functionDeclaration -> {
                            this.declarationStack.beginScope(false);
                            visitFunctionDeclarationHeader(functionDeclaration);
                            this.declarationStack.endScope();
                        }
                        default -> throw new IllegalStateException();
                    }
                });

        super.visit(unit);
    }

    @Override
    public void visit(FunctionDeclaration declaration) {
        if (declaration.isIntrinsic())
            return;

        var willBeNested = this.currentFunctionStack.size() > 1;
        // Add nested functions to outer scope, as if this were a variable declaration
        if (willBeNested)
            addDeclaration(declaration);

        beginFunctionDeclaration(declaration);
        if (isInNestedFunction())
            visitFunctionDeclarationHeader(declaration);
        visitFunctionDeclarationBody(declaration);

        this.declarationStack.endScope();
        this.currentFunctionStack.removeLast();
    }

    @Override
    public void visit(FunctionDeclaration.ParameterDeclaration parameter) {
        { // Don't want to visit parameter name here
            parameter.getTypeExpr().accept(this);
        }
    }

    @Override
    public void visit(FunctionInvocationExpression invocation) {
        super.visit(invocation);

        if (!(invocation.getTarget() instanceof IDeclarationReference reference))
            throw new ValidationException(invocation.getTarget().getSpan(), "Function invocation target must be a function");
    }

    @Override
    public void visit(StructDeclaration declaration) {
        validateNoDuplicates(declaration.getGenericParameters(), Comparator.comparing(GenericParameterDeclaration::getName), "Duplicate generic parameter name");

        this.declarationStack.beginStructScope(declaration);
        { // Don't want to visit struct name here
            declaration.getFields().forEach(p -> p.accept(this));
            declaration.getFunctions().forEach(p -> p.accept(this));
        }
        this.declarationStack.endScope();
    }

    @Override
    public void visit(StructDeclaration.FieldDeclaration declaration) {
        // Pre-process 2 did this already in the global namespace
        if (isInNestedFunction())
            declaration.getTypeExpr().accept(this);
    }

    @Override
    public void visit(ObjectCreationExpression expression) {
        super.visit(expression);

        var struct = this.resolvedReferences.get(expression.getTargetNameExpr());
        if (!(struct instanceof StructDeclaration))
            throw new ValidationException(expression.getTargetNameExpr().getSpan(), "Object creation target must be a struct");

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

        if (this.currentFunctionStack.size() == 1)
            throw new ValidationException(returnStatement.getSpan(), "Return statement outside of function");
    }

    @Override
    public void visit(VariableDeclaration declaration) {
        { // Don't want to visit name here
            var typeExpr = declaration.getTypeExpr();
            if (typeExpr != null)
                typeExpr.accept(this);
            declaration.getInitializerExpr().accept(this);
        }

        this.functionLocalsCount.merge(this.currentFunctionStack.getLast(), 1, Integer::sum);

        addDeclaration(declaration);
    }

    @Override
    public void visit(VariableAssignmentExpression expression) {
        super.visit(expression);

        if (!(expression.getTargetExpr() instanceof IDeclarationReference targetRef))
            throw new ValidationException(expression.getTargetExpr().getSpan(), "Invalid assignment target");
    }

    @Override
    public void visit(BlockStatement statement) {
        this.declarationStack.beginScope(true);
        super.visit(statement);
        this.declarationStack.endScope();
    }

    @Override
    public void visit(GenericParameterDeclaration declaration) {
        addDeclaration(declaration);
    }

    @Override
    public void visit(TypeExpression typeExpression) {
        var span = typeExpression.getSpan();
        var type = typeExpression.getName();
        var genericParameters = typeExpression.getGenericParameters();
        genericParameters.forEach(t -> t.accept(this));

        Statement decl;
        if (Prelude.isBuiltInTypeName(typeExpression.getName()))
            decl = Prelude.getStructForTypeName(typeExpression.getName());
        else
            decl = this.declarationStack.resolveIdentifier(type);

        if (decl == null)
            throw new ValidationException(span, "Unknown type " + type);
        if (!(decl instanceof StructDeclaration) && !(decl instanceof GenericParameterDeclaration))
            throw new ValidationException(span, "Type " + type + " is not a struct or builtin type");
        // Ensure all generic parameters are bound
        if (decl instanceof StructDeclaration struct) {
            var genericParameterDeclarations = struct.getGenericParameters();
            if (genericParameterDeclarations.size() != genericParameters.size())
                throw new ValidationException(Span.of(genericParameters, span), "Wrong number of generic parameters. Expected %d, got %d".formatted(genericParameterDeclarations.size(), genericParameters.size()));
        }
        if (decl instanceof GenericParameterDeclaration && !genericParameters.isEmpty())
            throw new ValidationException(span, "Generic parameter cannot have generic parameters");

        this.resolvedReferences.put(typeExpression, decl);
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        var decl = this.declarationStack.resolveIdentifier(identifierExpression.getValue());
        if (decl == null)
            decl = Prelude.getGlobalFunction(identifierExpression.getValue());
        if (decl == null)
            throw new ValidationException(identifierExpression.getSpan(), "Unresolved identifier");

        if (!(decl instanceof VariableDeclaration) &&
            !(decl instanceof FunctionDeclaration) &&
            !(decl instanceof FunctionDeclaration.ParameterDeclaration) &&
            !(decl instanceof StructDeclaration))
            throw new ValidationException(identifierExpression.getSpan(), "Identifier is not a variable, function or struct");

        this.resolvedReferences.put(identifierExpression, decl);
    }

    private void visitFunctionDeclarationHeader(FunctionDeclaration declaration) {
        validateNoDuplicates(declaration.getGenericParameters(), Comparator.comparing(GenericParameterDeclaration::getName), "Duplicate generic parameter name");
        declaration.getGenericParameters().forEach(p -> p.accept(this));

        validateNoDuplicates(declaration.getParameters(), Comparator.comparing(p -> p.getNameExpr().getValue()), "Duplicate parameter name");
        declaration.getParameters().forEach(p -> p.accept(this));

        var returnTypeExpr = declaration.getReturnTypeExpr();
        if (returnTypeExpr != null)
            returnTypeExpr.accept(this);
    }

    private void visitFunctionDeclarationBody(FunctionDeclaration declaration) {
        // Parameters
        declaration.getGenericParameters().forEach(this::addDeclaration);
        declaration.getParameters().forEach(this::addDeclaration);

        var currentStruct = this.declarationStack.getEnclosingStruct();
        if (currentStruct != null) {
            var selfParameter = new FunctionDeclaration.ParameterDeclaration(
                    null,
                    new TypeExpression(null,
                            currentStruct.getNameExpr().getValue(),
                            currentStruct.getGenericParameters().stream()
                                    .map(p -> {
                                        var typeExpr = new TypeExpression(null, p.getName());
                                        this.resolvedReferences.put(typeExpr, p);
                                        return typeExpr;
                                    })
                                    .collect(Collectors.toList())
                    ),
                    new IdentifierExpression(null, "self")
            );
            addDeclaration(selfParameter);
            this.resolvedReferences.put(selfParameter.getTypeExpr(), currentStruct);
        }

        // Add self to inner scope, to allow nested functions to access themselves
        if (isInNestedFunction())
            addDeclaration(declaration);
        declaration.getBody().accept(this);
    }

    private <T extends Statement> void validateNoDuplicates(List<T> genericParameterDeclarations, Comparator<T> comparator, String message) {
        for (var p1 : genericParameterDeclarations) {
            for (var p2 : genericParameterDeclarations) {
                if (p1 == p2)
                    continue;
                if (comparator.compare(p1, p2) == 0)
                    throw new ValidationException(p2.getSpan(), "Duplicate generic parameter name");
            }
        }
    }

    private void addDeclaration(Statement declaration) {
        var name = switch (declaration) {
            case IVariableDeclaration decl -> decl.getNameExpr().getValue();
            case ITopLevelDeclaration decl -> decl.getNameExpr().getValue();
            case GenericParameterDeclaration decl -> decl.getName();
            default -> throw new IllegalArgumentException();
        };
        this.declarationStack.addDeclaration(name, declaration);
    }

    private void beginFunctionDeclaration(FunctionDeclaration declaration) {
        this.currentFunctionStack.addLast(declaration);
        this.declarationStack.beginScope(declaration.isInstanceFunction());
        this.functionLocalsCount.put(declaration, 0);
    }

    private boolean isInNestedFunction() {
        return this.currentFunctionStack.size() > 2;
    }
}
