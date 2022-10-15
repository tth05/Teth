package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public final class DoubleLiteralExpression extends Expression implements IDeclarationReference {

    private final double value;

    public DoubleLiteralExpression(Span span, double value) {
        super(span);
        this.value = value;
    }

    public double getValue() {
        return this.value;
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

        DoubleLiteralExpression that = (DoubleLiteralExpression) o;

        return Double.compare(that.value, this.value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(this.value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(Double.toString(this.value)).append("D");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
