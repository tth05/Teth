package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.ExpressionList;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.List;

public class ListLiteralExpression extends Expression {

    private final ExpressionList initializers;

    public ListLiteralExpression(ISpan span, ExpressionList initializers) {
        super(span);
        this.initializers = initializers;
    }

    public List<Expression> getInitializers() {
        return this.initializers;
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

        ListLiteralExpression that = (ListLiteralExpression) o;

        return this.initializers.equals(that.initializers);
    }

    @Override
    public int hashCode() {
        return this.initializers.hashCode();
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("[");
        this.initializers.forEach(i -> i.dump(builder));
        builder.append("]");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
