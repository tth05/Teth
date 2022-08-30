package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class BinaryExpression extends Expression {

    private Expression left;
    private Expression right;

    private Operator operator;

    public BinaryExpression(ISpan span, Expression left, Expression right, Operator operator) {
        super(span);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public BinaryExpression(Expression left, Expression right, Operator operator) {
        this(Span.of(left.getSpan(), right.getSpan()), left, right, operator);
    }

    public void setLeft(Expression left) {
        this.left = left;
        setSpan(Span.of(left.getSpan(), getSpan()));
    }

    public Expression getLeft() {
        return this.left;
    }

    public void setRight(Expression right) {
        this.right = right;
        setSpan(Span.of(getSpan(), right.getSpan()));
    }

    public Expression getRight() {
        return this.right;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
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

    public enum Operator {
        OP_POW(0),
        OP_MULTIPLY(1),
        OP_DIVIDE(1),
        OP_ADD(2),
        OP_SUBTRACT(2),
        OP_LESS(3),
        OP_LESS_EQUAL(3),
        OP_GREATER(3),
        OP_GREATER_EQUAL(3),
        OP_EQUAL(4),
        OP_NOT_EQUAL(4),
        OP_AND(5),
        OP_OR(6);

        private final int precedence;

        Operator(int precedence) {
            this.precedence = precedence;
        }

        public int getPrecedence() {
            return this.precedence;
        }

        /**
         * @return {@code true} if this operator evaluates to a boolean value, {@code false} otherwise.
         */
        public boolean producesBoolean() {
            return this == OP_EQUAL || this == OP_NOT_EQUAL || this == OP_LESS || this == OP_LESS_EQUAL ||
                   this == OP_GREATER || this == OP_GREATER_EQUAL || this == OP_AND || this == OP_OR;
        }

        public static Operator fromTokenType(TokenType type) {
            return switch (type) {
                case POW -> Operator.OP_POW;
                case MULTIPLY -> Operator.OP_MULTIPLY;
                case SLASH -> Operator.OP_DIVIDE;
                case PLUS -> Operator.OP_ADD;
                case MINUS -> Operator.OP_SUBTRACT;
                case LESS -> Operator.OP_LESS;
                case LESS_EQUAL -> Operator.OP_LESS_EQUAL;
                case GREATER -> Operator.OP_GREATER;
                case GREATER_EQUAL -> Operator.OP_GREATER_EQUAL;
                case EQUAL_EQUAL -> Operator.OP_EQUAL;
                case NOT_EQUAL -> Operator.OP_NOT_EQUAL;
                case AMPERSAND_AMPERSAND -> Operator.OP_AND;
                case PIPE_PIPE -> Operator.OP_OR;
                default -> null;
            };
        }

        public String asString() {
            return switch (this) {
                case OP_POW -> "^";
                case OP_MULTIPLY -> "*";
                case OP_DIVIDE -> "/";
                case OP_ADD -> "+";
                case OP_SUBTRACT -> "-";
                case OP_LESS -> "<";
                case OP_LESS_EQUAL -> "<=";
                case OP_GREATER -> ">";
                case OP_GREATER_EQUAL -> ">=";
                case OP_EQUAL -> "==";
                case OP_NOT_EQUAL -> "!=";
                case OP_AND -> "&&";
                case OP_OR -> "||";
            };
        }
    }

}


