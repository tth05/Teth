package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.stdlib.StandardLibrary;

import java.util.*;

public class Analyzer {

    public static final FunctionDeclaration GLOBAL_FUNCTION = new FunctionDeclaration(null, null, List.of(), List.of(), null, null, false);

    private final Map<Expression, Type> resolvedExpressionTypes = new IdentityHashMap<>();
    private final Map<Expression, GenericParameterInfo> genericParameterInfos = new IdentityHashMap<>();
    private final Map<IDeclarationReference, Statement> resolvedReferences = new IdentityHashMap<>();
    private final Map<FunctionDeclaration, Integer> functionLocalsCount = new IdentityHashMap<>();

    private final SourceFileUnit unit;

    public Analyzer(SourceFileUnit unit) {
        this.unit = unit;
    }

    public ProblemList analyze() {
        try {
            new Visitor().visit(this.unit);
            return ProblemList.of();
        } catch (TypeResolverException | ValidationException e) {
            return ProblemList.of(e.asProblem());
        }
    }

    public Statement resolvedReference(IDeclarationReference identifierExpression) {
        return this.resolvedReferences.get(identifierExpression);
    }

    public Type resolvedType(Expression expression) {
        return this.resolvedExpressionTypes.get(expression);
    }

    public int functionLocalsCount(FunctionDeclaration function) {
        return Objects.requireNonNull(this.functionLocalsCount.getOrDefault(function, null), "Function locals count not set");
    }

    @SuppressWarnings("UnqualifiedFieldAccess")
    private final class Visitor extends ASTVisitor {

        private static final Map<Type, BinaryExpression.Operator[]> BINARY_OPERATORS_ALLOWED_TYPES = new HashMap<>();
        static {
            var all = BinaryExpression.Operator.values();
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.LONG, all);
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.DOUBLE, all);
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.BOOLEAN, new BinaryExpression.Operator[]{
                    BinaryExpression.Operator.OP_EQUAL, BinaryExpression.Operator.OP_NOT_EQUAL,
                    BinaryExpression.Operator.OP_AND, BinaryExpression.Operator.OP_OR
            });
        }

        private final DeclarationStack declarationStack = new DeclarationStack();
        private final Deque<FunctionDeclaration> currentFunctionStack = new ArrayDeque<>(5);

        private StructDeclaration currentStruct;

        @Override
        public void visit(SourceFileUnit unit) {
            // Begin global scope
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
            var nested = this.currentFunctionStack.size() > 1;
            // Add nested functions to outer scope, as if this were a variable declaration
            if (nested)
                addDeclaration(declaration);

            beginFunctionDeclaration(declaration);

            if (nested)
                visitFunctionDeclarationHeader(declaration);
            visitFunctionDeclarationBody(declaration);

            this.declarationStack.endScope();
            this.currentFunctionStack.removeLast();
        }

        private void visitFunctionDeclarationHeader(FunctionDeclaration declaration) {
            for (var p1 : declaration.getGenericParameters()) {
                for (var p2 : declaration.getGenericParameters()) {
                    if (p1 == p2)
                        continue;
                    if (p1.getName().equals(p2.getName()))
                        throw new ValidationException(p2.getSpan(), "Duplicate generic parameter name");
                }
            }
            declaration.getGenericParameters().forEach(p -> p.accept(this));

            declaration.getParameters().forEach(p -> p.accept(this));

            var returnTypeExpr = declaration.getReturnTypeExpr();
            if (returnTypeExpr != null)
                returnTypeExpr.accept(this);
        }

        private void visitFunctionDeclarationBody(FunctionDeclaration declaration) {
            var oldCurrentStruct = this.currentStruct;
            this.currentStruct = null;

            // Parameters
            declaration.getGenericParameters().forEach(this::addDeclaration);
            declaration.getParameters().forEach(this::addDeclaration);

            if (oldCurrentStruct != null) {
                addDeclaration(new FunctionDeclaration.ParameterDeclaration(
                        null,
                        new TypeExpression(null, oldCurrentStruct.getNameExpr().getValue()),
                        new IdentifierExpression(null, "self")
                ));
            }

            // Add self to inner scope, to allow nested functions to access themselves
            if (this.currentFunctionStack.size() > 1)
                addDeclaration(declaration);
            // TODO: Validate body returns something in all cases
            declaration.getBody().accept(this);

            if (declaration.getReturnTypeExpr() != null) {
                ScopeExitHelper.validateLastChildReturns(declaration.getBody()).ifPresent(offendingStatement -> {
                    throw new ValidationException(offendingStatement.getSpan(), "Block needs to return in all cases");
                });
            }

            this.currentStruct = oldCurrentStruct;
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

            var func = resolvedReferences.get(reference);
            if (!(func instanceof FunctionDeclaration decl))
                throw new ValidationException(invocation.getTarget().getSpan(), "Function invocation target must be a function");

            if (decl.getParameters().size() != invocation.getParameters().size())
                throw new ValidationException(invocation.getSpan(), "Wrong number of parameters for function invocation. Expected " + decl.getParameters().size() + ", got " + invocation.getParameters().size());

            GenericParameterInfo genericParameterInfo = null;
            if (!decl.getGenericParameters().isEmpty()) {
                genericParameterInfos.put(invocation, genericParameterInfo = new GenericParameterInfo());

                var genericBounds = invocation.getGenericBounds();
                // Explicit generic bounds have priority over inferred generic bounds
                if (genericBounds != null) {
                    if (genericBounds.size() != decl.getGenericParameters().size())
                        throw new ValidationException(invocation.getSpan(), "Wrong number of generic bounds. Expected " + decl.getGenericParameters().size() + ", got " + genericBounds.size());

                    for (var i = 0; i < genericBounds.size(); i++)
                        genericParameterInfo.bindGenericParameter(decl.getGenericParameters().get(i).getName(), genericBounds.get(i).asType());
                }
            }

            for (int i = 0; i < decl.getParameters().size(); i++) {
                var param = decl.getParameters().get(i);
                var paramTypeExpr = param.getTypeExpr();
                var expression = invocation.getParameters().get(i);
                var expressionType = resolvedExpressionTypes.get(expression);

                var paramType = resolveTypeOrBind(genericParameterInfo, paramTypeExpr, expressionType);

                if (!expressionType.isSubtypeOf(paramType))
                    throw new ValidationException(expression.getSpan(), "Parameter type mismatch. Expected " + paramType + ", got " + expressionType);
            }

            var returnType = decl.getReturnTypeExpr() != null ? resolveTypeOrBind(genericParameterInfo, decl.getReturnTypeExpr(), null) : Type.VOID;
            if (returnType == null)
                throw new ValidationException(invocation.getSpan(), "Return type is generic, but not bound");

            resolvedExpressionTypes.put(invocation, returnType);
        }

        @Override
        public void visit(StructDeclaration declaration) {
            this.currentStruct = declaration;
            { // Don't want to visit struct name here
                declaration.getFields().forEach(p -> p.accept(this));
                declaration.getFunctions().forEach(p -> p.accept(this));
            }
            this.currentStruct = null;
        }

        @Override
        public void visit(StructDeclaration.FieldDeclaration declaration) {
            // Pre-process 2 did this already
        }

        @Override
        public void visit(ObjectCreationExpression expression) {
            super.visit(expression);

            var type = resolvedExpressionTypes.get(expression.getTargetNameExpr());
            var struct = resolvedReferences.get(expression.getTargetNameExpr());
            if (!(struct instanceof StructDeclaration structDeclaration))
                throw new ValidationException(expression.getTargetNameExpr().getSpan(), "Object creation target must be a struct");

            var parameters = expression.getParameters();
            if (parameters.size() != structDeclaration.getFields().size())
                throw new ValidationException(
                        parameters.getSpanOrDefault(expression.getSpan()),
                        "Wrong number of parameters for object creation. Expected " + structDeclaration.getFields().size() + ", got " + parameters.size()
                );

            for (int i = 0; i < parameters.size(); i++) {
                var parameterType = resolvedExpressionTypes.get(parameters.get(i));
                var fieldType = structDeclaration.getFields().get(i).getTypeExpr().asType();

                if (!parameterType.isSubtypeOf(fieldType))
                    throw new ValidationException(
                            parameters.get(i).getSpan(),
                            "Parameter type mismatch. Expected " + fieldType + ", got " + parameterType
                    );
            }

            resolvedReferences.put(expression, struct);
            resolvedExpressionTypes.put(expression, type);
        }

        @Override
        public void visit(MemberAccessExpression expression) {
            { // Don't want to visit member name here
                expression.getTarget().accept(this);
            }

            var type = resolvedExpressionTypes.get(expression.getTarget());
            Statement member;
            if (StandardLibrary.isBuiltinType(type)) {
                member = StandardLibrary.getMemberOfType(type, expression.getMemberNameExpr().getValue());
            } else {
                var decl = this.declarationStack.resolveIdentifier(type.toString());
                if (!(decl instanceof StructDeclaration structDeclaration))
                    throw new IllegalStateException("Declaration not found");

                member = structDeclaration.getMember(expression.getMemberNameExpr().getValue());
            }

            if (member == null)
                throw new TypeResolverException(expression.getMemberNameExpr().getSpan(), "Member " + expression.getMemberNameExpr().getValue() + " not found in type " + type);

            resolvedExpressionTypes.put(expression, switch (member) {
                case FunctionDeclaration ignored -> Type.FUNCTION;
                case StructDeclaration.FieldDeclaration field -> field.getTypeExpr().asType();
                default -> throw new IllegalStateException();
            });
            resolvedReferences.put(expression, member);
        }

        @Override
        public void visit(ReturnStatement returnStatement) {
            super.visit(returnStatement);

            if (this.currentFunctionStack.size() == 1)
                throw new ValidationException(returnStatement.getSpan(), "Return statement outside of function");

            var valueExpr = returnStatement.getValueExpr();
            var type = valueExpr == null ? Type.VOID : resolvedExpressionTypes.get(valueExpr);
            var currentFunction = this.currentFunctionStack.getLast();
            if (!type.isSubtypeOf(currentFunction.getReturnType()))
                throw new TypeResolverException(returnStatement.getSpan(), "Cannot return " + type + " from function returning " + currentFunction.getReturnType());
        }

        @Override
        public void visit(VariableDeclaration declaration) {
            { // Don't want to visit name here
                var typeExpr = declaration.getTypeExpr();
                if (typeExpr != null)
                    typeExpr.accept(this);
                declaration.getInitializerExpr().accept(this);
            }

            // This will increase the count even if a local is re-declared, but that's fine for now.
            functionLocalsCount.merge(this.currentFunctionStack.getLast(), 1, Integer::sum);

            var varTypeExpr = declaration.getTypeExpr();
            var expression = declaration.getInitializerExpr();
            if (varTypeExpr != null) {
                var resolvedType = resolvedExpressionTypes.get(expression);
                if (!resolvedType.isSubtypeOf(varTypeExpr.asType()))
                    throw new TypeResolverException(expression.getSpan(), "Cannot assign value of type " + resolvedType + " to variable of type " + varTypeExpr);
            }

            addDeclaration(declaration);
        }

        @Override
        public void visit(VariableAssignmentExpression expression) {
            super.visit(expression);

            var decl = resolvedReferences.get(expression.getTargetExpr());
            if (decl == null)
                throw new ValidationException(expression.getTargetExpr().getSpan(), "Unresolved identifier");
            if (!(decl instanceof IVariableDeclaration varDecl))
                throw new ValidationException(expression.getTargetExpr().getSpan(), "Identifier is not a variable");

            var type = resolvedExpressionTypes.get(expression.getExpr());
            if (!type.equals(getVariableDeclarationType(varDecl)))
                throw new TypeResolverException(expression.getExpr().getSpan(), "Cannot assign expression of type " + type + " to variable of type " + getVariableDeclarationType(varDecl));

            resolvedExpressionTypes.put(expression, type);
        }

        @Override
        public void visit(BlockStatement statement) {
            this.declarationStack.beginScope(true);
            super.visit(statement);
            this.declarationStack.endScope();
        }

        @Override
        public void visit(IfStatement statement) {
            super.visit(statement);

            if (!resolvedExpressionTypes.get(statement.getCondition()).equals(Type.BOOLEAN))
                throw new TypeResolverException(statement.getCondition().getSpan(), "Condition of if statement must be a bool");
        }

        @Override
        public void visit(LoopStatement statement) {
            super.visit(statement);

            if (statement.getCondition() != null && !resolvedExpressionTypes.get(statement.getCondition()).equals(Type.BOOLEAN))
                throw new TypeResolverException(statement.getCondition().getSpan(), "Condition of loop statement must be a bool");
        }

        @Override
        public void visit(UnaryExpression expression) {
            super.visit(expression);

            var type = resolvedExpressionTypes.get(expression.getExpression());
            var operator = expression.getOperator();
            if ((operator == UnaryExpression.Operator.OP_NEGATIVE && !type.isNumber()) ||
                (operator == UnaryExpression.Operator.OP_NOT && type != Type.BOOLEAN))
                throw new TypeResolverException(expression.getExpression().getSpan(), "Unary operator " + operator.asString() + " cannot be applied to " + type);

            resolvedExpressionTypes.put(expression, type);
        }

        @Override
        public void visit(BinaryExpression expression) {
            super.visit(expression);

            var leftType = resolvedExpressionTypes.get(expression.getLeft());
            var rightType = resolvedExpressionTypes.get(expression.getRight());
            var anyNumber = leftType.isNumber() || rightType.isNumber();
            var typesMatch = anyNumber ? leftType.isNumber() && rightType.isNumber() : leftType.equals(rightType);

            if (!typesMatch ||
                !BINARY_OPERATORS_ALLOWED_TYPES.containsKey(leftType) ||
                Arrays.stream(BINARY_OPERATORS_ALLOWED_TYPES.get(leftType)).noneMatch(op -> op == expression.getOperator()))
//                throw new TypeResolverException(expression.getSpan(), "Operator " + expression.getOperator().asString() + " cannot be applied to " + leftType + " and " + rightType);
                // TODO: This check is disabled for now because list.get returns any. This need normal array access or generics.
                Objects.equals(true, true);

            Type binaryType;
            if (expression.getOperator().producesBoolean())
                binaryType = Type.BOOLEAN;
            else if (anyNumber)
                binaryType = leftType == Type.DOUBLE || rightType == Type.DOUBLE ? Type.DOUBLE : Type.LONG;
            else
                binaryType = leftType;

            resolvedExpressionTypes.put(expression, binaryType);
        }

        @Override
        public void visit(ListLiteralExpression listLiteralExpression) {
            super.visit(listLiteralExpression);

            if (listLiteralExpression.getInitializers().isEmpty()) {
                resolvedExpressionTypes.put(listLiteralExpression, Type.list(Type.ANY));
                return;
            }

            var elementType = resolvedExpressionTypes.get(listLiteralExpression.getInitializers().get(0));
            for (Expression initializer : listLiteralExpression.getInitializers()) {
                if (!resolvedExpressionTypes.get(initializer).isSubtypeOf(elementType))
                    throw new TypeResolverException(initializer.getSpan(), "List element type mismatch");
            }

            resolvedExpressionTypes.put(listLiteralExpression, Type.list(elementType));
        }

        @Override
        public void visit(BooleanLiteralExpression booleanLiteralExpression) {
            resolvedExpressionTypes.put(booleanLiteralExpression, Type.BOOLEAN);
        }

        @Override
        public void visit(StringLiteralExpression stringLiteralExpression) {
            resolvedExpressionTypes.put(stringLiteralExpression, Type.STRING);
        }

        @Override
        public void visit(DoubleLiteralExpression doubleLiteralExpression) {
            resolvedExpressionTypes.put(doubleLiteralExpression, Type.DOUBLE);
        }

        @Override
        public void visit(LongLiteralExpression longLiteralExpression) {
            resolvedExpressionTypes.put(longLiteralExpression, Type.LONG);
        }

        @Override
        public void visit(GenericParameterDeclaration declaration) {
            addDeclaration(declaration);
        }

        @Override
        public void visit(TypeExpression typeExpression) {
            validateType(typeExpression);
        }

        private void validateType(TypeExpression typeExpression) {
            var span = typeExpression.getSpan();
            var type = typeExpression.getName();
            typeExpression.getGenericBounds().forEach(this::validateType);

            if (StandardLibrary.isBuiltinType(typeExpression.asType()))
                return;

            var decl = this.declarationStack.resolveIdentifier(type);
            if (decl == null)
                throw new TypeResolverException(span, "Unknown type " + type);
            if (!(decl instanceof StructDeclaration) && !(decl instanceof GenericParameterDeclaration))
                throw new TypeResolverException(span, "Type " + type + " is not a struct or builtin type");

            resolvedReferences.put(typeExpression, decl);
        }

        @Override
        public void visit(IdentifierExpression identifierExpression) {
            var decl = this.declarationStack.resolveIdentifier(identifierExpression.getValue());
            if (decl == null)
                decl = StandardLibrary.getGlobalFunction(identifierExpression.getValue());
            if (decl == null)
                throw new ValidationException(identifierExpression.getSpan(), "Unresolved identifier");

            var type = switch (decl) {
                case VariableDeclaration declaration -> getVariableDeclarationType(declaration);
                case FunctionDeclaration ignored -> Type.FUNCTION;
                case FunctionDeclaration.ParameterDeclaration declaration -> declaration.getTypeExpr().asType();
                case StructDeclaration structDeclaration -> new Type(structDeclaration.getNameExpr().getValue());
                default ->
                        throw new ValidationException(identifierExpression.getSpan(), "Identifier is not a variable, function or struct");
            };

            resolvedExpressionTypes.put(identifierExpression, type);
            resolvedReferences.put(identifierExpression, decl);
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

        private Type resolveTypeOrBind(GenericParameterInfo genericParameterInfo, TypeExpression typeExpr, Type fallbackType) {
            Type paramType;
            if (referencesGenericParameter(typeExpr)) {
                var actualType = genericParameterInfo.getBoundGenericParameter(typeExpr.getName());
                if (actualType == null) {
                    if (fallbackType == null)
                        return null;

                    genericParameterInfo.bindGenericParameter(typeExpr.getName(), actualType = fallbackType);
                }
                paramType = actualType;
            } else {
                // This resolves inner generic parameters when converting the expression to a type. Allows list<T> to
                // become list<long>
                paramType = typeExpr.asType((expr) -> {
                    if (referencesGenericParameter(expr))
                        return resolveTypeOrBind(genericParameterInfo, expr, null);
                    else
                        return expr.asType();
                });
            }

            return paramType;
        }

        private boolean referencesGenericParameter(TypeExpression typeExpression) {
            return resolvedReferences.get(typeExpression) instanceof GenericParameterDeclaration;
        }

        private Type getVariableDeclarationType(IVariableDeclaration declaration) {
            if (declaration.getTypeExpr() != null) {
                return declaration.getTypeExpr().asType();
            } else {
                if (!(declaration instanceof VariableDeclaration varDecl))
                    throw new IllegalStateException();
                return resolvedExpressionTypes.get(varDecl.getInitializerExpr());
            }
        }

        private void beginFunctionDeclaration(FunctionDeclaration declaration) {
            this.currentFunctionStack.addLast(declaration);
            this.declarationStack.beginScope(declaration.isInstanceFunction());
            functionLocalsCount.put(declaration, 0);
        }
    }
}
