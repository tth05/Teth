package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class DoubleLiteralExpression extends Expression {

    private final double value;

    public DoubleLiteralExpression(ISpan span, double value) {
        super(span);
        this.value = value;
    }

    public double getValue() {
        return this.value;
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