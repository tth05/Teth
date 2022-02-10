package com.github.tth05.teth.lang.parser.ast;

public class LongLiteralExpression extends Expression {

    private final long value;

    public LongLiteralExpression(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "LongLiteralExpression{" +
               "value=" + value +
               '}';
    }
}
