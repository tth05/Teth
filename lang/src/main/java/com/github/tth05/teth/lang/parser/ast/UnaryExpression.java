package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public final class UnaryExpression extends Expression {

    private final Expression expression;

    private final Operator operator;

    public UnaryExpression(Span span, Expression expression, Operator operator) {
        super(span);
        this.expression = expression;
        this.operator = operator;
    }

    public enum Operator {
        OP_NEGATIVE,
        OP_NOT;

        public static Operator fromTokenType(TokenType type) {
            return switch (type) {
                case MINUS -> Operator.OP_NEGATIVE;
                case NOT -> Operator.OP_NOT;
                default -> null;
            };
        }

        public String asString() {
            return switch (this) {
                case OP_NEGATIVE -> "-";
                case OP_NOT -> "!";
            };
        }
    }

    public Expression getExpression() {
        return this.expression;
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
        builder.append("UnaryExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("expression");
        this.expression.dump(builder);
        builder.newLine().appendAttribute("operator")
                .append("'")
                .append(this.operator.asString())
                .append("'")
                .append(" ")
                .append(this.operator.name());
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UnaryExpression that = (UnaryExpression) o;

        if (!this.expression.equals(that.expression))
            return false;
        return this.operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = this.expression.hashCode();
        result = 31 * result + this.operator.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
