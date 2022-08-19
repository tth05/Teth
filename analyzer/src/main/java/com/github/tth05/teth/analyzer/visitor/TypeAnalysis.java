package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.analyzer.GenericParameterInfo;
import com.github.tth05.teth.analyzer.TypeResolverException;
import com.github.tth05.teth.analyzer.ValidationException;
import com.github.tth05.teth.analyzer.type.SemanticType;
import com.github.tth05.teth.analyzer.type.TypeCache;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.ExpressionList;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.*;
import java.util.function.Function;

import static com.github.tth05.teth.analyzer.prelude.Prelude.*;

public class TypeAnalysis extends ASTVisitor {

    private final TypeCache typeCache = new TypeCache();
    private final Map<Integer, BinaryExpression.Operator[]> binaryOperatorsAllowedTypes = new HashMap<>();
    {
        var all = BinaryExpression.Operator.values();
        this.binaryOperatorsAllowedTypes.put(this.typeCache.internalizeType(LONG_STRUCT_DECLARATION), all);
        this.binaryOperatorsAllowedTypes.put(this.typeCache.internalizeType(DOUBLE_STRUCT_DECLARATION), all);
        this.binaryOperatorsAllowedTypes.put(this.typeCache.internalizeType(BOOLEAN_STRUCT_DECLARATION), new BinaryExpression.Operator[]{
                BinaryExpression.Operator.OP_EQUAL, BinaryExpression.Operator.OP_NOT_EQUAL,
                BinaryExpression.Operator.OP_AND, BinaryExpression.Operator.OP_OR
        });
    }

    private final Deque<FunctionDeclaration> currentFunctionStack = new ArrayDeque<>(5);
    private final Map<Expression, SemanticType> resolvedExpressionTypes = new IdentityHashMap<>();
    private final Map<Expression, GenericParameterInfo> genericParameterInfos = new IdentityHashMap<>();

    private final Map<IDeclarationReference, Statement> resolvedReferences;

    public TypeAnalysis(Map<IDeclarationReference, Statement> resolvedReferences) {
        this.resolvedReferences = resolvedReferences;
    }

    @Override
    public void visit(StructDeclaration declaration) {
        declaration.getFunctions().forEach(p -> p.accept(this));
    }

    @Override
    public void visit(FunctionDeclaration declaration) {
        if (declaration.isIntrinsic())
            return;

        this.currentFunctionStack.addLast(declaration);

        visitFunctionDeclarationHeader(declaration);
        visitFunctionDeclarationBody(declaration);

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

        if (!(this.resolvedReferences.get(((IDeclarationReference) invocation.getTarget())) instanceof FunctionDeclaration decl))
            throw new ValidationException(invocation.getTarget().getSpan(), "Function invocation target must be a function");

        var genericParameterInfo = new GenericParameterInfo();
        // This inherits bound generic parameters from the struct instance which we're calling this function on
        if (decl.isInstanceFunction()) {
            // Instance functions will always have a member access as their parent
            var targetType = this.resolvedExpressionTypes.get(((MemberAccessExpression) invocation.getTarget()).getTarget());
            var struct = (StructDeclaration) this.typeCache.getDeclaration(targetType);
            for (int i = 0; i < struct.getGenericParameters().size(); i++) {
                var genericParameter = struct.getGenericParameters().get(i);
                genericParameterInfo.bindGenericParameter(genericParameter.getName(), targetType.getGenericBounds().get(i));
            }
        }

        genericParameterInfo = typeCheckInvocationExpression(
                invocation,
                genericParameterInfo,
                decl.getGenericParameters(), invocation.getGenericParameters(),
                decl.getParameters(), invocation.getParameters()
        );

        var returnType = decl.getReturnTypeExpr() != null ? resolveTypeOrBind(genericParameterInfo, decl.getReturnTypeExpr(), null) : this.typeCache.voidType();
        if (returnType == null)
            throw new TypeResolverException(invocation.getSpan(), "Return type is generic, but not bound");

        this.resolvedExpressionTypes.put(invocation, returnType);
    }

    @Override
    public void visit(ObjectCreationExpression expression) {
        super.visit(expression);

        var structDeclaration = (StructDeclaration) this.resolvedReferences.get(expression.getTargetNameExpr());

        var genericParameterInfo = typeCheckInvocationExpression(
                expression,
                new GenericParameterInfo(),
                structDeclaration.getGenericParameters(), expression.getGenericParameters(),
                structDeclaration.getFields(), expression.getParameters()
        );

        // These will allow us to construct a new type for the struct with all generic parameters resolved
        var resolvedGenericParameters = structDeclaration.getGenericParameters().isEmpty() ?
                null :
                structDeclaration.getGenericParameters().stream().map(p -> {
                    var bound = genericParameterInfo.getBoundGenericParameter(p.getName());
                    if (bound == null)
                        throw new IllegalStateException();
                    return bound;
                }).toList();

        this.resolvedExpressionTypes.put(
                expression,
                resolvedGenericParameters == null ?
                        this.typeCache.newType(structDeclaration) :
                        new SemanticType(this.typeCache.internalizeType(structDeclaration), resolvedGenericParameters)
        );
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        super.visit(returnStatement);

        var valueExpr = returnStatement.getValueExpr();
        var returnType = valueExpr == null ? this.typeCache.voidType() : this.resolvedExpressionTypes.get(valueExpr);
        var currentFunction = this.currentFunctionStack.getLast();
        var functionReturnType = currentFunction.getReturnTypeExpr() == null ? this.typeCache.voidType() : asType(currentFunction.getReturnTypeExpr());
        if (!this.typeCache.isSubtypeOf(returnType, functionReturnType))
            throw new TypeResolverException(returnStatement.getSpan(), "Cannot return " + this.typeCache.toString(returnType) + " from function returning " + this.typeCache.toString(functionReturnType));
    }

    @Override
    public void visit(VariableDeclaration declaration) {
        { // Don't want to visit name here
            declaration.getInitializerExpr().accept(this);
        }

        var varTypeExpr = declaration.getTypeExpr();
        var expression = declaration.getInitializerExpr();
        if (varTypeExpr != null) {
            var resolvedType = this.resolvedExpressionTypes.get(expression);
            if (!this.typeCache.isSubtypeOf(resolvedType, asType(varTypeExpr)))
                throw new TypeResolverException(expression.getSpan(), "Cannot assign value of type " + this.typeCache.toString(resolvedType) + " to variable of type " + varTypeExpr);
        }
    }

    @Override
    public void visit(VariableAssignmentExpression expression) {
        super.visit(expression);

        // Can only be done after type resolution, therefore not contained in NameAnalysis
        if (!(this.resolvedReferences.get((IDeclarationReference) expression.getTargetExpr()) instanceof IVariableDeclaration varDecl))
            throw new TypeResolverException(expression.getSpan(), "Invalid assignment target");

        var type = this.resolvedExpressionTypes.get(expression.getExpr());
        if (!type.equals(getVariableDeclarationType(varDecl)))
            throw new TypeResolverException(expression.getExpr().getSpan(), "Cannot assign expression of type " + this.typeCache.toString(type) + " to variable of type " + this.typeCache.toString(getVariableDeclarationType(varDecl)));

        this.resolvedExpressionTypes.put(expression, type);
    }

    @Override
    public void visit(MemberAccessExpression expression) {
        { // Don't want to visit member name here
            expression.getTarget().accept(this);
        }

        var type = this.resolvedExpressionTypes.get(expression.getTarget());
        var decl = this.typeCache.getDeclaration(type);
        if (!(decl instanceof StructDeclaration structDeclaration))
            throw new IllegalStateException("Declaration not found");

        var member = structDeclaration.getMember(expression.getMemberNameExpr().getValue());

        if (member == null)
            throw new TypeResolverException(expression.getMemberNameExpr().getSpan(), "Member " + expression.getMemberNameExpr().getValue() + " not found in type " + this.typeCache.toString(type));

        this.resolvedExpressionTypes.put(expression, switch (member) {
            case FunctionDeclaration ignored -> this.typeCache.voidType(); // Not implemented
            case StructDeclaration.FieldDeclaration field -> asType(field.getTypeExpr());
            default -> throw new IllegalStateException();
        });
        // Can only be done after type resolution, therefore not contained in NameAnalysis
        this.resolvedReferences.put(expression, member);
    }

    @Override
    public void visit(IfStatement statement) {
        super.visit(statement);

        if (!this.resolvedExpressionTypes.get(statement.getCondition()).equals(this.typeCache.newType(BOOLEAN_STRUCT_DECLARATION)))
            throw new TypeResolverException(statement.getCondition().getSpan(), "Condition of if statement must be a bool");
    }

    @Override
    public void visit(LoopStatement statement) {
        super.visit(statement);

        if (statement.getCondition() != null && !this.resolvedExpressionTypes.get(statement.getCondition()).equals(this.typeCache.newType(BOOLEAN_STRUCT_DECLARATION)))
            throw new TypeResolverException(statement.getCondition().getSpan(), "Condition of loop statement must be a bool");
    }

    @Override
    public void visit(UnaryExpression expression) {
        super.visit(expression);

        var type = this.resolvedExpressionTypes.get(expression.getExpression());
        var operator = expression.getOperator();
        if ((operator == UnaryExpression.Operator.OP_NEGATIVE && !this.typeCache.isNumber(type)) ||
            (operator == UnaryExpression.Operator.OP_NOT && type != this.typeCache.newType(BOOLEAN_STRUCT_DECLARATION)))
            throw new TypeResolverException(expression.getExpression().getSpan(), "Unary operator " + operator.asString() + " cannot be applied to " + this.typeCache.toString(type));

        this.resolvedExpressionTypes.put(expression, type);
    }

    @Override
    public void visit(BinaryExpression expression) {
        super.visit(expression);

        var leftType = this.resolvedExpressionTypes.get(expression.getLeft());
        var rightType = this.resolvedExpressionTypes.get(expression.getRight());
        var leftIsNumber = this.typeCache.isNumber(leftType);
        var rightIsNumber = this.typeCache.isNumber(rightType);
        var anyNumber = leftIsNumber || rightIsNumber;
        var typesMatch = anyNumber ? leftIsNumber && rightIsNumber : leftType.equals(rightType);

        if (!typesMatch ||
            !this.binaryOperatorsAllowedTypes.containsKey(leftType.getTypeId()) ||
            Arrays.stream(this.binaryOperatorsAllowedTypes.get(leftType.getTypeId())).noneMatch(op -> op == expression.getOperator()))
            throw new TypeResolverException(expression.getSpan(), "Operator " + expression.getOperator().asString() + " cannot be applied to " + this.typeCache.toString(leftType) + " and " + this.typeCache.toString(rightType));

        SemanticType binaryType;
        if (expression.getOperator().producesBoolean()) {
            binaryType = this.typeCache.newType(BOOLEAN_STRUCT_DECLARATION);
        } else if (anyNumber) {
            var doubleType = this.typeCache.newType(DOUBLE_STRUCT_DECLARATION);
            var longType = this.typeCache.newType(LONG_STRUCT_DECLARATION);
            binaryType = leftType == doubleType || rightType == doubleType ? doubleType : longType;
        } else {
            binaryType = leftType;
        }

        this.resolvedExpressionTypes.put(expression, binaryType);
    }

    @Override
    public void visit(ListLiteralExpression listLiteralExpression) {
        super.visit(listLiteralExpression);

        if (listLiteralExpression.getInitializers().isEmpty()) {
            this.resolvedExpressionTypes.put(listLiteralExpression, this.typeCache.newGenericType(LIST_STRUCT_DECLARATION, ANY_STRUCT_DECLARATION));
            return;
        }

        var elementType = this.resolvedExpressionTypes.get(listLiteralExpression.getInitializers().get(0));
        for (Expression initializer : listLiteralExpression.getInitializers()) {
            if (!this.typeCache.isSubtypeOf(this.resolvedExpressionTypes.get(initializer), elementType))
                throw new TypeResolverException(initializer.getSpan(), "List element type mismatch");
        }

        this.resolvedExpressionTypes.put(listLiteralExpression, new SemanticType(this.typeCache.internalizeType(LIST_STRUCT_DECLARATION), List.of(elementType)));
    }

    @Override
    public void visit(BooleanLiteralExpression booleanLiteralExpression) {
        this.resolvedExpressionTypes.put(booleanLiteralExpression, this.typeCache.newType(BOOLEAN_STRUCT_DECLARATION));
    }

    @Override
    public void visit(StringLiteralExpression stringLiteralExpression) {
        this.resolvedExpressionTypes.put(stringLiteralExpression, this.typeCache.newType(STRING_STRUCT_DECLARATION));
    }

    @Override
    public void visit(DoubleLiteralExpression doubleLiteralExpression) {
        this.resolvedExpressionTypes.put(doubleLiteralExpression, this.typeCache.newType(DOUBLE_STRUCT_DECLARATION));
    }

    @Override
    public void visit(LongLiteralExpression longLiteralExpression) {
        this.resolvedExpressionTypes.put(longLiteralExpression, this.typeCache.newType(LONG_STRUCT_DECLARATION));
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        var decl = this.resolvedReferences.get(identifierExpression);

        var type = (SemanticType) switch (decl) {
            case VariableDeclaration declaration -> getVariableDeclarationType(declaration);
            case FunctionDeclaration ignored -> null;
            case FunctionDeclaration.ParameterDeclaration declaration -> asType(declaration.getTypeExpr());
            case StructDeclaration structDeclaration -> null;
            default -> throw new IllegalStateException();
        };

        if (type == null)
            return;

        this.resolvedExpressionTypes.put(identifierExpression, type);
    }

    private void visitFunctionDeclarationHeader(FunctionDeclaration declaration) {
        declaration.getParameters().forEach(p -> p.accept(this));
        var expr = declaration.getReturnTypeExpr();
        if (expr != null)
            expr.accept(this);
    }

    private void visitFunctionDeclarationBody(FunctionDeclaration declaration) {
        declaration.getBody().accept(this);
    }

    private SemanticType resolveTypeOrBind(GenericParameterInfo genericParameterInfo, TypeExpression typeExpr, SemanticType fallbackType) {
        SemanticType paramType;
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
            paramType = asType(typeExpr, (expr) -> {
                if (referencesGenericParameter(expr))
                    return resolveTypeOrBind(genericParameterInfo, expr, null);
                else
                    return asType(expr);
            });
        }

        return paramType;
    }

    private <T extends IVariableDeclaration> GenericParameterInfo typeCheckInvocationExpression(
            Expression invocation,
            GenericParameterInfo genericParameterInfo,
            List<GenericParameterDeclaration> genericParameterDeclarations,
            List<TypeExpression> explicitGenericParameters,
            List<T> parameterDeclarations,
            ExpressionList parameters
    ) {
        if (!genericParameterDeclarations.isEmpty()) {
            this.genericParameterInfos.put(invocation, genericParameterInfo);

            // Explicit generic parameters have priority over inferred generic parameters
            if (!explicitGenericParameters.isEmpty()) {
                if (explicitGenericParameters.size() != genericParameterDeclarations.size())
                    throw new ValidationException(invocation.getSpan(), "Wrong number of generic bounds. Expected " + genericParameterDeclarations.size() + ", got " + explicitGenericParameters.size());

                for (var i = 0; i < explicitGenericParameters.size(); i++)
                    genericParameterInfo.bindGenericParameter(genericParameterDeclarations.get(i).getName(), asType(explicitGenericParameters.get(i)));
            }
        }

        if (parameterDeclarations.size() != parameters.size())
            throw new ValidationException(invocation.getSpan(), "Wrong number of parameters. Expected %d, got %d".formatted(parameterDeclarations.size(), parameters.size()));

        for (int i = 0; i < parameterDeclarations.size(); i++) {
            var param = parameterDeclarations.get(i);
            var paramTypeExpr = param.getTypeExpr();
            var expression = parameters.get(i);
            var expressionType = this.resolvedExpressionTypes.get(expression);

            var paramType = resolveTypeOrBind(genericParameterInfo, paramTypeExpr, expressionType);

            if (!this.typeCache.isSubtypeOf(expressionType, paramType))
                throw new TypeResolverException(expression.getSpan(), "Parameter type mismatch. Expected " + this.typeCache.toString(paramType) + ", got " + this.typeCache.toString(expressionType));
        }

        for (var genericParameterDeclaration : genericParameterDeclarations) {
            if (!genericParameterInfo.isGenericParameterBound(genericParameterDeclaration.getName()))
                throw new TypeResolverException(invocation.getSpan(), "Generic parameter " + genericParameterDeclaration.getName() + " is not bound");
        }

        return genericParameterInfo;
    }

    private SemanticType asType(TypeExpression typeExpr) {
        return asType(typeExpr, expr -> this.typeCache.newType(this.resolvedReferences.get(expr)));
    }

    private SemanticType asType(TypeExpression typeExpression, Function<TypeExpression, SemanticType> basicTypeFactory) {
        var ref = this.resolvedReferences.get(typeExpression);

        if (typeExpression.getGenericParameters().isEmpty())
            return basicTypeFactory.apply(typeExpression);

        var genericParameters = typeExpression.getGenericParameters().stream()
                .map(p -> asType(p, basicTypeFactory))
                .toList();

        return new SemanticType(this.typeCache.internalizeType(ref), genericParameters);
    }

    private boolean referencesGenericParameter(TypeExpression typeExpression) {
        return this.resolvedReferences.get(typeExpression) instanceof GenericParameterDeclaration;
    }

    private SemanticType getVariableDeclarationType(IVariableDeclaration declaration) {
        if (declaration.getTypeExpr() != null) {
            return asType(declaration.getTypeExpr());
        } else {
            if (!(declaration instanceof VariableDeclaration varDecl))
                throw new IllegalStateException();
            return this.resolvedExpressionTypes.get(varDecl.getInitializerExpr());
        }
    }

    private boolean isInNestedFunction() {
        return this.currentFunctionStack.size() > 2;
    }
}
