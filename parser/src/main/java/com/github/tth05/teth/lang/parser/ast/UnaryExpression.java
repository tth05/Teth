package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class UnaryExpression extends Expression {

    private final Expression expression;

    private final Operator operator;

    public UnaryExpression(Expression expression, Operator operator) {
        this.expression = expression;
        this.operator = operator;
    }

    public enum Operator {
        OP_NEGATIVE;

        public static Operator fromTokenType(TokenType type) {
            return switch (type) {
                case OP_MINUS -> Operator.OP_NEGATIVE;
                default -> throw new IllegalArgumentException();
            };
        }

        public String asString() {
            return switch (this) {
                case OP_NEGATIVE -> "-";
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
                .append(this.operator.name())
                .newLine();
        builder.endBlock();
        builder.newLine().append("}");
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
