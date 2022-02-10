package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.lexer.TokenType;

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
        OP_POW;

        public static Operator fromTokenType(TokenType type) {
            return switch (type) {
                case OP_PLUS -> Operator.OP_ADD;
                case OP_MINUS -> Operator.OP_SUBTRACT;
                case OP_STAR -> Operator.OP_MULTIPLY;
                case OP_SLASH -> Operator.OP_DIVIDE;
                case OP_ROOF -> Operator.OP_POW;
                default -> throw new IllegalArgumentException();
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
    public String toString() {
        return "BinaryExpression{" +
               "left=" + this.left +
               ", operator=" + this.operator +
               ", right=" + this.right +
               '}';
    }
}
