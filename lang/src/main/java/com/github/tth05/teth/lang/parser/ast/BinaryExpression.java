package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class BinaryExpression extends Expression {

    private final Expression left;
    private final Expression right;

    private final Operator operator;

    public BinaryExpression(Expression left, Expression right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public enum Operator {
        OP_ADD,
        OP_SUBTRACT,
        OP_MULTIPLY,
        OP_DIVIDE,
        OP_POW,
        OP_EQUAL;

        public static Operator fromTokenType(TokenType type) {
            return switch (type) {
                case OP_PLUS -> Operator.OP_ADD;
                case OP_MINUS -> Operator.OP_SUBTRACT;
                case OP_STAR -> Operator.OP_MULTIPLY;
                case OP_SLASH -> Operator.OP_DIVIDE;
                case OP_ROOF -> Operator.OP_POW;
                case OP_EQUAL -> Operator.OP_EQUAL;
                default -> throw new IllegalArgumentException();
            };
        }

        public String asString() {
            return switch (this) {
                case OP_ADD -> "+";
                case OP_SUBTRACT -> "-";
                case OP_MULTIPLY -> "*";
                case OP_DIVIDE -> "/";
                case OP_POW -> "^";
                case OP_EQUAL -> "==";
            };
        }
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("BinaryExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("left");
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
        builder.endBlock();
        builder.newLine().append("}");
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
