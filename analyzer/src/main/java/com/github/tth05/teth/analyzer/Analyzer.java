package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.stdlib.StandardLibrary;

import java.util.*;

public class Analyzer {

    private final Map<Expression, Type> resolvedExpressionTypes = new IdentityHashMap<>();
    private final Map<IDeclarationReference, Statement> resolvedReferences = new IdentityHashMap<>();

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

    /**
     * IntrinsicFunctionBinding, SourceFunctionBinding
     */
    public Statement resolvedIdentifierBinding(IdentifierExpression identifierExpression) {
        return this.resolvedReferences.get(identifierExpression);
    }

    public Type resolvedType(Expression expression) {
        return this.resolvedExpressionTypes.get(expression);
    }

    @SuppressWarnings("UnqualifiedFieldAccess")
    private final class Visitor extends ASTVisitor {

        private static final Map<Type, BinaryExpression.Operator[]> BINARY_OPERATORS_ALLOWED_TYPES = new HashMap<>();
        static {
            var all = BinaryExpression.Operator.values();
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.LONG, all);
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.DOUBLE, all);
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.STRING, new BinaryExpression.Operator[]{BinaryExpression.Operator.OP_ADD});
            BINARY_OPERATORS_ALLOWED_TYPES.put(Type.BOOLEAN, new BinaryExpression.Operator[]{BinaryExpression.Operator.OP_EQUAL, BinaryExpression.Operator.OP_NOT_EQUAL});
        }

        private final DeclarationStack declarationStack = new DeclarationStack();

        private Deque<FunctionDeclaration> currentFunctionStack = new ArrayDeque<>(5);

        @Override
        public void visit(SourceFileUnit unit) {
            // Begin global scope
            this.declarationStack.beginScope(false);
            // Collect top level functions
            for (var decl : unit.getStatements()) {
                if (!(decl instanceof FunctionDeclaration functionDeclaration))
                    continue;

                this.declarationStack.addDeclaration(functionDeclaration.getNameExpr().getValue(), functionDeclaration);
            }

            super.visit(unit);
        }

        @Override
        public void visit(FunctionDeclaration declaration) {
            // Add nested functions to outer scope, as if this were a variable declaration
            if (!this.currentFunctionStack.isEmpty())
                this.declarationStack.addDeclaration(declaration.getNameExpr().getValue(), declaration);

            this.currentFunctionStack.addLast(declaration);
            this.declarationStack.beginScope(false);
            // Don't want to visit function name here
            declaration.getParameters().forEach(p -> p.accept(this));
            var returnTypeExpr = declaration.getReturnTypeExpr();
            if (returnTypeExpr != null)
                returnTypeExpr.accept(this);

            // Add self to inner scope, to allow nested functions to access themselves
            if (this.currentFunctionStack.size() > 1)
                this.declarationStack.addDeclaration(declaration.getNameExpr().getValue(), declaration);
            // TODO: Validate body returns something in all cases
            declaration.getBody().accept(this);
            this.declarationStack.endScope();
            this.currentFunctionStack.removeLast();
        }

        @Override
        public void visit(FunctionDeclaration.ParameterDeclaration parameter) {
            { // Don't want to visit parameter name here
                parameter.getTypeExpr().accept(this);
            }

            this.declarationStack.addDeclaration(parameter.getNameExpr().getValue(), parameter);
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

            for (int i = 0; i < decl.getParameters().size(); i++) {
                var param = decl.getParameters().get(i);
                var paramType = param.getTypeExpr().getType();
                var paramExpr = invocation.getParameters().get(i);
                var paramExprType = resolvedExpressionTypes.get(paramExpr);
                if (!paramType.equals(paramExprType))
                    throw new ValidationException(paramExpr.getSpan(), "Parameter type mismatch. Expected " + paramType + ", got " + paramExprType);
            }

            resolvedExpressionTypes.put(invocation, decl.getReturnType());
        }

        @Override
        public void visit(MemberAccessExpression expression) {
            { // Don't want to visit member name here
                expression.getTarget().accept(this);
            }

            var type = resolvedExpressionTypes.get(expression.getTarget());
            var member = StandardLibrary.getMemberOfType(type, expression.getMemberNameExpr().getValue());
            if (member == null)
                throw new TypeResolverException(expression.getMemberNameExpr().getSpan(), "Member " + expression.getMemberNameExpr().getValue() + " not found in type " + type);

            resolvedExpressionTypes.put(expression, switch (member) {
                case FunctionDeclaration ignored -> Type.FUNCTION;
                default -> throw new IllegalStateException();
            });
            resolvedReferences.put(expression, member);
        }

        @Override
        public void visit(ReturnStatement returnStatement) {
            super.visit(returnStatement);

            if (this.currentFunctionStack.isEmpty())
                throw new ValidationException(returnStatement.getSpan(), "Return statement outside of function");

            var type = resolvedExpressionTypes.get(returnStatement.getValueExpr());
            var currentFunction = this.currentFunctionStack.getLast();
            if (!type.equals(currentFunction.getReturnType()))
                throw new TypeResolverException(returnStatement.getSpan(), "Cannot return " + type + " from function returning " + currentFunction.getReturnType());
        }

        @Override
        public void visit(VariableDeclaration declaration) {
            { // Don't want to visit name here
                declaration.getTypeExpr().accept(this);
                var expression = declaration.getExpression();
                if (expression != null)
                    expression.accept(this);
            }

            var type = declaration.getTypeExpr().getType();
            var expression = declaration.getExpression();
            if (expression != null) {
                var resolvedType = resolvedExpressionTypes.get(expression);
                if (!resolvedType.isSubtypeOf(type))
                    throw new TypeResolverException(expression.getSpan(), "Cannot assign value of type " + resolvedType + " to variable of type " + type);
            }

            this.declarationStack.addDeclaration(declaration.getNameExpr().getValue(), declaration);
        }

        @Override
        public void visit(VariableAssignmentExpression expression) {
            super.visit(expression);

            var decl = resolvedReferences.get(expression.getTargetNameExpr());

            if (decl == null)
                throw new ValidationException(expression.getTargetNameExpr().getSpan(), "Unresolved identifier");
            if (!(decl instanceof IVariableDeclaration varDecl))
                throw new ValidationException(expression.getTargetNameExpr().getSpan(), "Identifier is not a variable");

            var type = resolvedExpressionTypes.get(expression.getExpr());
            if (!type.equals(varDecl.getTypeExpr().getType()))
                throw new TypeResolverException(expression.getExpr().getSpan(), "Cannot assign expression of type " + type + " to variable of type " + varDecl.getTypeExpr().getType());

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
                throw new TypeResolverException(expression.getSpan(), "Operator " + expression.getOperator().asString() + " cannot be applied to " + leftType + " and " + rightType);

            var binaryType = anyNumber ? (leftType == Type.DOUBLE || rightType == Type.DOUBLE ? Type.DOUBLE : Type.LONG) : leftType;
            resolvedExpressionTypes.put(expression, binaryType);
        }

        @Override
        public void visit(ListLiteralExpression listLiteralExpression) {
            super.visit(listLiteralExpression);

            if (listLiteralExpression.getInitializers().isEmpty()) {
                resolvedExpressionTypes.put(listLiteralExpression, new Type(Type.ANY));
                return;
            }

            var elementType = resolvedExpressionTypes.get(listLiteralExpression.getInitializers().get(0));
            for (Expression initializer : listLiteralExpression.getInitializers()) {
                if (!resolvedExpressionTypes.get(initializer).isSubtypeOf(elementType))
                    throw new TypeResolverException(initializer.getSpan(), "List element type mismatch");
            }

            resolvedExpressionTypes.put(listLiteralExpression, new Type(elementType));
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
        public void visit(TypeExpression typeExpression) {
            if (!StandardLibrary.isBuiltinType(typeExpression.getType()))
                throw new TypeResolverException(typeExpression.getSpan(), "Unknown type " + typeExpression.getType());
        }

        @Override
        public void visit(IdentifierExpression identifierExpression) {
            var decl = this.declarationStack.resolveIdentifier(identifierExpression.getValue());
            if (decl == null)
                throw new ValidationException(identifierExpression.getSpan(), "Unresolved identifier");

            var type = switch (decl) {
                case VariableDeclaration declaration -> declaration.getTypeExpr().getType();
                case FunctionDeclaration ignored -> Type.FUNCTION;
                case FunctionDeclaration.ParameterDeclaration declaration -> declaration.getTypeExpr().getType();
                default ->
                        throw new ValidationException(identifierExpression.getSpan(), "Identifier is not a variable or function");
            };

            resolvedExpressionTypes.put(identifierExpression, type);
            resolvedReferences.put(identifierExpression, decl);
        }
    }
}
