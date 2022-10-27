package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.analyzer.type.SemanticType;
import com.github.tth05.teth.analyzer.type.TypeCache;
import com.github.tth05.teth.lang.parser.ExpressionList;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;

import java.util.*;

import static com.github.tth05.teth.analyzer.prelude.Prelude.*;

public class TypeAnalysis extends AnalysisASTVisitor {

    private final Map<Integer, BinaryExpression.Operator[]> binaryOperatorsAllowedTypes = new HashMap<>();

    private final Deque<FunctionDeclaration> currentFunctionStack = new ArrayDeque<>(5);

    private final Map<IDeclarationReference, Statement> resolvedReferences;
    private final Map<Expression, SemanticType> resolvedExpressionTypes;
    private final TypeCache typeCache;

    public TypeAnalysis(TypeCache typeCache, Map<IDeclarationReference, Statement> resolvedReferences, Map<Expression, SemanticType> resolvedExpressionTypes) {
        this.typeCache = typeCache;
        this.resolvedReferences = resolvedReferences;
        this.resolvedExpressionTypes = resolvedExpressionTypes;

        var all = BinaryExpression.Operator.values();
        this.binaryOperatorsAllowedTypes.put(this.typeCache.internalizeType(LONG_STRUCT_DECLARATION), all);
        this.binaryOperatorsAllowedTypes.put(this.typeCache.internalizeType(DOUBLE_STRUCT_DECLARATION), all);
        this.binaryOperatorsAllowedTypes.put(this.typeCache.internalizeType(BOOLEAN_STRUCT_DECLARATION), new BinaryExpression.Operator[]{
                BinaryExpression.Operator.OP_EQUAL, BinaryExpression.Operator.OP_NOT_EQUAL,
                BinaryExpression.Operator.OP_AND, BinaryExpression.Operator.OP_OR
        });
        this.binaryOperatorsAllowedTypes.put(SemanticType.NULL.getTypeId(), new BinaryExpression.Operator[]{
                BinaryExpression.Operator.OP_EQUAL, BinaryExpression.Operator.OP_NOT_EQUAL
        });
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

        if (!(invocation.getTarget() instanceof IDeclarationReference))
            return;

        var targetReference = this.resolvedReferences.get(((IDeclarationReference) invocation.getTarget()));
        if (targetReference == null)
            return;
        if (!(targetReference instanceof FunctionDeclaration decl)) {
            report(invocation.getTarget().getSpan(), "Function invocation target must be a function");
            return;
        }

        var genericParameterInfo = new GenericParameterInfo();
        // This inherits bound generic parameters from the struct instance which we're calling this function on
        if (decl.isInstanceFunction()) {
            // Instance functions will always have a member access as their parent
            var targetType = this.resolvedExpressionTypes.get(((MemberAccessExpression) invocation.getTarget()).getTarget());
            var struct = (StructDeclaration) this.typeCache.getDeclaration(targetType);
            for (int i = 0; i < struct.getGenericParameters().size(); i++) {
                var genericParameter = struct.getGenericParameters().get(i);
                genericParameterInfo.bindGenericParameter(genericParameter.getNameExpr().getValue(), targetType.getGenericBounds().get(i));
            }
        }

        typeCheckInvocationExpression(
                invocation,
                genericParameterInfo,
                decl.getGenericParameters(), invocation.getGenericParameters(),
                decl.getParameters(), invocation.getParameters()
        );

        var returnType = decl.getReturnTypeExpr() != null ? asTypeGeneric(genericParameterInfo, decl.getReturnTypeExpr(), null) : this.typeCache.voidType();
        if (returnType == null) {
            report(invocation.getSpan(), "Return type is generic, but not bound");
            return;
        }

        this.resolvedExpressionTypes.put(invocation, returnType);
    }

    @Override
    public void visit(ObjectCreationExpression expression) {
        super.visit(expression);

        if (!(this.resolvedReferences.get(expression.getTargetNameExpr()) instanceof StructDeclaration structDeclaration)) {
            report(expression.getTargetNameExpr().getSpan(), "Cannot instantiate this type");
            return;
        }

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
                    var bound = genericParameterInfo.getBoundGenericParameter(p.getNameExpr().getValue());
                    if (bound == null)
                        throw new IllegalStateException();
                    return bound;
                }).toList();

        this.resolvedExpressionTypes.put(
                expression,
                resolvedGenericParameters == null ?
                        this.typeCache.getType(structDeclaration) :
                        new SemanticType(this.typeCache.internalizeType(structDeclaration), resolvedGenericParameters)
        );
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        super.visit(returnStatement);

        var valueExpr = returnStatement.getValueExpr();
        var returnType = valueExpr == null ? this.typeCache.voidType() : this.resolvedExpressionTypes.get(valueExpr);
        if (returnType == null)
            return;

        if (this.currentFunctionStack.isEmpty())
            return;

        var currentFunction = this.currentFunctionStack.getLast();
        var functionReturnType = currentFunction.getReturnTypeExpr() == null ? this.typeCache.voidType() : asType(currentFunction.getReturnTypeExpr());
        if (!this.typeCache.isSubtypeOf(returnType, functionReturnType))
            report(returnStatement.getSpan(), "Cannot return " + this.typeCache.toString(returnType) + " from function returning " + this.typeCache.toString(functionReturnType));
    }

    @Override
    public void visit(VariableDeclaration declaration) {
        { // Don't want to visit name here
            declaration.getInitializerExpr().accept(this);
            if (declaration.getTypeExpr() != null)
                declaration.getTypeExpr().accept(this);
        }

        if (hasErrorFlag()) {
            clearErrorFlag();
            return;
        }

        var varTypeExpr = declaration.getTypeExpr();
        var expression = declaration.getInitializerExpr();
        var resolvedType = this.resolvedExpressionTypes.get(expression);
        if (resolvedType == null) {
            report(expression.getSpan(), "Cannot determine type of expression");
            return;
        }

        if (varTypeExpr != null) {
            if (!this.typeCache.isSubtypeOf(resolvedType, asType(varTypeExpr)))
                report(expression.getSpan(), "Cannot assign value of type " + this.typeCache.toString(resolvedType) + " to variable of type " + varTypeExpr);
        }
    }

    @Override
    public void visit(MemberAccessExpression expression) {
        { // Don't want to visit member name here
            expression.getTarget().accept(this);
        }

        var type = this.resolvedExpressionTypes.get(expression.getTarget());
        if (type == null) {
            report(expression.getTarget().getSpan(), "Target does not have any members");
            return;
        }

        var decl = this.typeCache.getDeclaration(type);
        if (!(decl instanceof StructDeclaration structDeclaration)) {
            report(expression.getTarget().getSpan(), "Target does not have any members");
            return;
        }

        var member = structDeclaration.getMember(expression.getMemberNameExpr().getValue());

        if (member == null) {
            report(expression.getMemberNameExpr().getSpan(), "Member " + expression.getMemberNameExpr().getValue() + " not found in type " + this.typeCache.toString(type));
            return;
        }

        // TODO: Switch preview disabled
        SemanticType resolvedType;
        if (member instanceof FunctionDeclaration) {
            resolvedType = this.typeCache.voidType(); // Not implemented
        } else if (member instanceof StructDeclaration.FieldDeclaration field) {
            var reference = this.resolvedReferences.get(field.getTypeExpr());
            // Infer field type if it is generic
            if (reference instanceof GenericParameterDeclaration)
                resolvedType = type.getGenericBounds().get(structDeclaration.getGenericParameters().indexOf(reference));
            else
                resolvedType = asType(field.getTypeExpr());
        } else {
            throw new IllegalStateException();
        }

        this.resolvedExpressionTypes.put(expression, resolvedType);
        // Can only be done after type resolution, therefore not contained in NameAnalysis
        this.resolvedReferences.put(expression, member);
        this.resolvedReferences.put(expression.getMemberNameExpr(), member);
    }

    @Override
    public void visit(IfStatement statement) {
        super.visit(statement);

        if (!this.typeCache.getType(BOOLEAN_STRUCT_DECLARATION).equals(this.resolvedExpressionTypes.get(statement.getCondition())))
            report(statement.getCondition().getSpan(), "Condition of if statement must be a bool");
    }

    @Override
    public void visit(LoopStatement statement) {
        super.visit(statement);

        if (statement.getCondition() != null && !this.typeCache.getType(BOOLEAN_STRUCT_DECLARATION).equals(this.resolvedExpressionTypes.get(statement.getCondition())))
            report(statement.getCondition().getSpan(), "Condition of loop statement must be a bool");
    }

    @Override
    public void visit(ParenthesisedExpression expression) {
        super.visit(expression);

        var type = this.resolvedExpressionTypes.get(expression.getExpression());
        if (type == null)
            return;

        this.resolvedExpressionTypes.put(expression, type);
    }

    @Override
    public void visit(UnaryExpression expression) {
        super.visit(expression);

        var type = this.resolvedExpressionTypes.get(expression.getExpression());
        if (type == null)
            return;

        var operator = expression.getOperator();
        if ((operator == UnaryExpression.Operator.OP_NEGATE && !this.typeCache.isNumber(type)) ||
            (operator == UnaryExpression.Operator.OP_NOT && type != this.typeCache.getType(BOOLEAN_STRUCT_DECLARATION))) {
            report(expression.getExpression().getSpan(), "Unary operator " + operator.asString() + " cannot be applied to " + this.typeCache.toString(type));
            return;
        }

        this.resolvedExpressionTypes.put(expression, type);
    }

    @Override
    public void visit(BinaryExpression expression) {
        super.visit(expression);

        if (hasErrorFlag()) {
            clearErrorFlag();
            return;
        }

        if (expression.getOperator() == BinaryExpression.Operator.OP_ASSIGN) {
            visitAssignmentExpression(expression);
            return;
        }

        var leftType = this.resolvedExpressionTypes.get(expression.getLeft());
        if (leftType == null) {
            report(expression.getLeft().getSpan(), "Cannot determine type of expression");
            return;
        }

        var rightType = this.resolvedExpressionTypes.get(expression.getRight());
        if (rightType == null) {
            report(expression.getRight().getSpan(), "Cannot determine type of expression");
            return;
        }

        var leftIsNumber = this.typeCache.isNumber(leftType);
        var rightIsNumber = this.typeCache.isNumber(rightType);
        var anyNumber = leftIsNumber || rightIsNumber;
        var typesMatch = anyNumber ? leftIsNumber && rightIsNumber : leftType.equals(rightType);
        // Specific checks for comparing with null
        if (!typesMatch && (leftType == SemanticType.NULL || rightType == SemanticType.NULL)) {
            // Check for == and !=
            if (Arrays.stream(this.binaryOperatorsAllowedTypes.get(SemanticType.NULL.getTypeId())).noneMatch(op -> op == expression.getOperator())) {
                report(expression.getSpan(), "Operator " + expression.getOperator().asString() + " cannot be applied to " + this.typeCache.toString(leftType) + " and " + this.typeCache.toString(rightType));
                return;
            }

            // Disallow null comparisons for non-nullable types
            var nonNullType = leftType == SemanticType.NULL ? rightType : leftType;
            if (nonNullType == this.typeCache.getType(LONG_STRUCT_DECLARATION) || nonNullType == this.typeCache.getType(DOUBLE_STRUCT_DECLARATION) || nonNullType == this.typeCache.getType(BOOLEAN_STRUCT_DECLARATION)) {
                report(expression.getSpan(), "Operator " + expression.getOperator().asString() + " cannot be applied to " + this.typeCache.toString(leftType) + " and " + this.typeCache.toString(rightType));
                return;
            }
        } else if (!typesMatch || // Make sure types match and operator is allowed for that type otherwise
            !this.binaryOperatorsAllowedTypes.containsKey(leftType.getTypeId()) ||
            Arrays.stream(this.binaryOperatorsAllowedTypes.get(leftType.getTypeId())).noneMatch(op -> op == expression.getOperator())) {
            report(expression.getSpan(), "Operator " + expression.getOperator().asString() + " cannot be applied to " + this.typeCache.toString(leftType) + " and " + this.typeCache.toString(rightType));
            return;
        }

        // Compute output type
        SemanticType binaryType;
        if (expression.getOperator().producesBoolean()) {
            binaryType = this.typeCache.getType(BOOLEAN_STRUCT_DECLARATION);
        } else if (anyNumber) {
            var doubleType = this.typeCache.getType(DOUBLE_STRUCT_DECLARATION);
            var longType = this.typeCache.getType(LONG_STRUCT_DECLARATION);
            binaryType = leftType == doubleType || rightType == doubleType ? doubleType : longType;
        } else {
            binaryType = leftType;
        }

        this.resolvedExpressionTypes.put(expression, binaryType);
    }

    public void visitAssignmentExpression(BinaryExpression expression) {
        if (!(expression.getLeft() instanceof IDeclarationReference))
            return;

        // Can only be done after type resolution, therefore not contained in NameAnalysis
        if (!(this.resolvedReferences.get((IDeclarationReference) expression.getLeft()) instanceof IVariableDeclaration varDecl)) {
            report(expression.getSpan(), "Invalid assignment target");
            clearErrorFlag();
            return;
        }

        var type = this.resolvedExpressionTypes.get(expression.getRight());
        if (type == null) {
            clearErrorFlag();
            return;
        }

        if (!this.typeCache.isSubtypeOf(type, getVariableDeclarationType(varDecl)))
            report(expression.getRight().getSpan(), "Cannot assign expression of type " + this.typeCache.toString(type) + " to variable of type " + this.typeCache.toString(getVariableDeclarationType(varDecl)));

        this.resolvedExpressionTypes.put(expression, type);
        clearErrorFlag();
    }

    @Override
    public void visit(ListLiteralExpression listLiteralExpression) {
        super.visit(listLiteralExpression);

        if (listLiteralExpression.getInitializers().isEmpty()) {
            this.resolvedExpressionTypes.put(listLiteralExpression, new SemanticType(this.typeCache.internalizeType(LIST_STRUCT_DECLARATION), List.of(this.typeCache.getType(ANY_STRUCT_DECLARATION))));
            return;
        }

        var elementType = this.resolvedExpressionTypes.get(listLiteralExpression.getInitializers().get(0));
        if (elementType == null)
            return;
        if (elementType == SemanticType.NULL) {
            report(listLiteralExpression.getInitializers().get(0).getSpan(), "Cannot infer type of list element");
            return;
        }

        for (Expression initializer : listLiteralExpression.getInitializers()) {
            if (!this.typeCache.isSubtypeOf(this.resolvedExpressionTypes.get(initializer), elementType))
                report(initializer.getSpan(), "List element type mismatch");
        }

        this.resolvedExpressionTypes.put(listLiteralExpression, new SemanticType(this.typeCache.internalizeType(LIST_STRUCT_DECLARATION), List.of(elementType)));
    }

    @Override
    public void visit(BooleanLiteralExpression booleanLiteralExpression) {
        this.resolvedExpressionTypes.put(booleanLiteralExpression, this.typeCache.getType(BOOLEAN_STRUCT_DECLARATION));
    }

    @Override
    public void visit(NullLiteralExpression doubleLiteralExpression) {
        this.resolvedExpressionTypes.put(doubleLiteralExpression, SemanticType.NULL);
    }

    @Override
    public void visit(StringLiteralExpression stringLiteralExpression) {
        super.visit(stringLiteralExpression);

        for (var part : stringLiteralExpression.getParts()) {
            if (part.getType() != StringLiteralExpression.PartType.EXPRESSION)
                continue;

            var type = this.resolvedExpressionTypes.get(part.asExpression());
            if (!this.typeCache.getType(STRING_STRUCT_DECLARATION).equals(type))
                report(part.asExpression().getSpan(), "String literal part must be a string");
        }

        this.resolvedExpressionTypes.put(stringLiteralExpression, this.typeCache.getType(STRING_STRUCT_DECLARATION));
    }

    @Override
    public void visit(DoubleLiteralExpression doubleLiteralExpression) {
        this.resolvedExpressionTypes.put(doubleLiteralExpression, this.typeCache.getType(DOUBLE_STRUCT_DECLARATION));
    }

    @Override
    public void visit(LongLiteralExpression longLiteralExpression) {
        this.resolvedExpressionTypes.put(longLiteralExpression, this.typeCache.getType(LONG_STRUCT_DECLARATION));
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        var decl = this.resolvedReferences.get(identifierExpression);

        SemanticType type;
        if (decl instanceof IVariableDeclaration varDecl) {
            type = getVariableDeclarationType(varDecl);
        } else if (decl == null || decl instanceof FunctionDeclaration || decl instanceof StructDeclaration || decl instanceof GenericParameterDeclaration) {
            type = null;
        } else {
            throw new IllegalStateException("Unexpected declaration type: " + decl.getClass().getName());
        }

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

    private SemanticType asTypeGeneric(GenericParameterInfo genericParameterInfo, TypeExpression typeExpr, SemanticType fallbackType) {
        SemanticType paramType;
        if (referencesGenericParameter(typeExpr)) {
            var actualType = genericParameterInfo.getBoundGenericParameter(typeExpr.getNameExpr().getValue());
            if (actualType == null) {
                if (fallbackType == null)
                    return null;

                genericParameterInfo.bindGenericParameter(typeExpr.getNameExpr().getValue(), actualType = fallbackType);
            }
            paramType = actualType;
        } else {
            // Non-generic reference with no parameters -> simply internalize it
            if (typeExpr.getGenericParameters().isEmpty())
                return asType(typeExpr);

            var ref = this.resolvedReferences.get(typeExpr);
            if (ref == null)
                return null;

            // This resolves inner generic parameters when converting the expression to a type. Allows list<T> to
            // become list<long>
            var genericParameters = new ArrayList<SemanticType>(typeExpr.getGenericParameters().size());
            var fallbackTypeGenericBounds = fallbackType == null || !fallbackType.hasGenericBounds() ? null : fallbackType.getGenericBounds();
            for (int i = 0; i < typeExpr.getGenericParameters().size(); i++) {
                // We "step into" the fallback type here to resolve the generic parameters of the inner type
                // Also note that the case of no fallback type is handled above, because the fallback type is only
                // important when the current expression references a generic parameter. Otherwise, which is the case
                // here, we can just propagate null or the fallback type without caring about the value.
                var fallbackTypeGenericBound = fallbackTypeGenericBounds == null ? null : fallbackTypeGenericBounds.get(i);

                var genericParamType = asTypeGeneric(genericParameterInfo, typeExpr.getGenericParameters().get(i), fallbackTypeGenericBound);
                if (genericParamType == null)
                    return null;

                genericParameters.add(genericParamType);
            }

            paramType = new SemanticType(
                    this.typeCache.internalizeType(ref),
                    genericParameters.isEmpty() ? null : genericParameters
            );
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
        if (!explicitGenericParameters.isEmpty()) {
            if (explicitGenericParameters.size() != genericParameterDeclarations.size()) {
                var span = Span.of(explicitGenericParameters, invocation.getSpan());
                report(span, "Wrong number of generic bounds. Expected " + genericParameterDeclarations.size() + ", got " + explicitGenericParameters.size());
                ensureAllGenericParametersBound(span, genericParameterInfo, genericParameterDeclarations);
                return genericParameterInfo;
            }
        }

        // Explicit generic parameters have priority over inferred generic parameters
        for (var i = 0; i < explicitGenericParameters.size(); i++) {
            genericParameterInfo.bindGenericParameter(genericParameterDeclarations.get(i).getNameExpr().getValue(), asType(explicitGenericParameters.get(i)));
        }

        if (parameterDeclarations.size() != parameters.size()) {
            var span = parameters.getSpanOrElse(invocation.getSpan());
            report(span, "Wrong number of parameters. Expected %d, got %d".formatted(parameterDeclarations.size(), parameters.size()));
            ensureAllGenericParametersBound(span, genericParameterInfo, genericParameterDeclarations);
            return genericParameterInfo;
        }

        for (int i = 0; i < parameterDeclarations.size(); i++) {
            var param = parameterDeclarations.get(i);
            var paramTypeExpr = param.getTypeExpr();
            var expression = parameters.get(i);
            var expressionType = this.resolvedExpressionTypes.get(expression);

            var paramType = asTypeGeneric(genericParameterInfo, paramTypeExpr, expressionType);

            if (paramType == null)
                report(expression.getSpan(), "Parameter type mismatch. Expected " + paramTypeExpr + ", got " + this.typeCache.toString(expressionType));
            else if (!this.typeCache.isSubtypeOf(expressionType, paramType))
                report(expression.getSpan(), "Parameter type mismatch. Expected " + this.typeCache.toString(paramType) + ", got " + this.typeCache.toString(expressionType));
        }

        ensureAllGenericParametersBound(Span.of(explicitGenericParameters, invocation.getSpan()), genericParameterInfo, genericParameterDeclarations);
        return genericParameterInfo;
    }

    private void ensureAllGenericParametersBound(Span span, GenericParameterInfo genericParameterInfo, List<GenericParameterDeclaration> genericParameterDeclarations) {
        for (var genericParameterDeclaration : genericParameterDeclarations) {
            var name = genericParameterDeclaration.getNameExpr().getValue();
            if (!genericParameterInfo.isGenericParameterBound(name)) {
                report(span, "Generic parameter " + genericParameterDeclaration.getNameExpr() + " is not bound");
                genericParameterInfo.bindGenericParameter(name, SemanticType.UNRESOLVED);
            }
        }
    }

    private SemanticType asType(TypeExpression typeExpr) {
        var ref = this.resolvedReferences.get(typeExpr);

        if (ref == null)
            return SemanticType.UNRESOLVED;
        if (typeExpr.getGenericParameters().isEmpty())
            return this.typeCache.getType(ref);

        var genericParameters = typeExpr.getGenericParameters().stream()
                .map(this::asType)
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

    private static class GenericParameterInfo {

        private Map<String, SemanticType> boundGenericParameters;

        public boolean isGenericParameterBound(String name) {
            return this.boundGenericParameters != null && this.boundGenericParameters.containsKey(name);
        }

        public void bindGenericParameter(String name, SemanticType type) {
            if (this.boundGenericParameters == null)
                this.boundGenericParameters = new HashMap<>(3);

            this.boundGenericParameters.put(name, type);
        }

        public SemanticType getBoundGenericParameter(String name) {
            if (this.boundGenericParameters == null)
                return null;

            return this.boundGenericParameters.get(name);
        }
    }
}
