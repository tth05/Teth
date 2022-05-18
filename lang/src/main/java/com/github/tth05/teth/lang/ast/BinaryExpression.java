package com.github.tth05.teth.lang.ast;

import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class BinaryExpression extends Expression {

    private final Expression left;
    private Expression right;

    private final Operator operator;

    public BinaryExpression(Expression left, Expression right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public enum Operator {
        OP_EQUAL(0),
        OP_NOT_EQUAL(0),
        OP_LESS(0),
        OP_LESS_EQUAL(0),
        OP_GREATER(0),
        OP_GREATER_EQUAL(0),
        OP_POW(1),
        OP_MULTIPLY(2),
        OP_DIVIDE(2),
        OP_ADD(3),
        OP_SUBTRACT(3),
        OP_ASSIGN(4);

        private final int precedence;

        Operator(int precedence) {
            this.precedence = precedence;
        }

        public int getPrecedence() {
            return this.precedence;
        }

        public static Operator fromTokenType(TokenType type) {
            return switch (type) {
                case EQUAL_EQUAL -> Operator.OP_EQUAL;
                case NOT_EQUAL -> Operator.OP_NOT_EQUAL;
                case LESS -> Operator.OP_LESS;
                case LESS_EQUAL -> Operator.OP_LESS_EQUAL;
                case GREATER -> Operator.OP_GREATER;
                case GREATER_EQUAL -> Operator.OP_GREATER_EQUAL;
                case POW -> Operator.OP_POW;
                case MULTIPLY -> Operator.OP_MULTIPLY;
                case DIVIDE -> Operator.OP_DIVIDE;
                case PLUS -> Operator.OP_ADD;
                case MINUS -> Operator.OP_SUBTRACT;
                case EQUAL -> Operator.OP_ASSIGN;
                default -> null;
            };
        }

        public String asString() {
            return switch (this) {
                case OP_EQUAL -> "==";
                case OP_NOT_EQUAL -> "!=";
                case OP_LESS -> "<";
                case OP_LESS_EQUAL -> "<=";
                case OP_GREATER -> ">";
                case OP_GREATER_EQUAL -> ">=";
                case OP_POW -> "^";
                case OP_MULTIPLY -> "*";
                case OP_DIVIDE -> "/";
                case OP_ADD -> "+";
                case OP_SUBTRACT -> "-";
                case OP_ASSIGN -> "=";
            };
        }
    }

    public Expression getLeft() {
        return this.left;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public Expression getRight() {
        return this.right;
    }

    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("BinaryExpression {").newLine().startBlock()
                .appendAttribute("left");
        this.left.dump(builder);
        builder.newLine().appendAttribute("operator")
                .append("'")
                .append(this.operator.asString())
                .append("'")
                .append(" ")
                .append(this.operator.name())
                .newLine();
        builder.appendAttribute("right");
        this.right.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BinaryExpression that = (BinaryExpression) o;

        if (!this.left.equals(that.left))
            return false;
        if (!this.right.equals(that.right))
            return false;
        return this.operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = this.left.hashCode();
        result = 31 * result + this.right.hashCode();
        result = 31 * result + this.operator.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
