package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class Analyzer {

    private final Map<Expression, Type> resolvedExpressionTypes = new IdentityHashMap<>();

    private final SourceFileUnit unit;

    public Analyzer(SourceFileUnit unit) {
        this.unit = unit;
    }

    public ProblemList analyze() {
        try {
            new Visitor().visit(this.unit);
            return ProblemList.of();
        } catch (TypeResolverException e) {
            return ProblemList.of(e.asProblem());
        }
    }

    /**
     * IntrinsicFunctionBinding, SourceFunctionBinding
     */
    public void resolvedInvocationBinding(FunctionInvocationExpression invocation) {

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

        @Override
        public void visit(VariableDeclaration declaration) {
            super.visit(declaration);

            var type = declaration.getTypeExpr().getType();
            var expression = declaration.getExpression();
            if (expression != null) {
                var resolvedType = resolvedExpressionTypes.get(expression);
                if (!resolvedType.isSubtypeOf(type))
                    throw new TypeResolverException(expression.getSpan(), "Cannot assign value of type " + resolvedType + " to variable of type " + type);
            }
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
    }
}
