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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return this.value == ((LongLiteralExpression) o).value;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (this.value ^ (this.value >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LongLiteralExpression{" +
               "value=" + this.value +
               '}';
    }
}
