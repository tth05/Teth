package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class ParenthesisedExpression extends Expression {

    private final Expression expression;

    public ParenthesisedExpression(Span span, Expression expression) {
        super(span);
        this.expression = expression;
    }

    public Expression getExpression() {
        return this.expression;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("ParenthesisedExpression {").newLine().startBlock().appendAttribute("expression");
        this.expression.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ParenthesisedExpression that = (ParenthesisedExpression) o;

        return this.expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return this.expression.hashCode();
    }
}
