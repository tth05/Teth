package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class BooleanLiteralExpression extends Expression {

    private final boolean value;

    public BooleanLiteralExpression(ISpan span, boolean value) {
        super(span);
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BooleanLiteralExpression that = (BooleanLiteralExpression) o;

        return this.value == that.value;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.value + "");
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(this.value);
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
