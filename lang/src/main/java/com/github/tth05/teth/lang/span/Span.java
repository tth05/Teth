package com.github.tth05.teth.lang.span;

public final class Span implements ISpan {

    private final char[] source;
    private final int offset;
    private final int line;
    private final int column;

    public Span(char[] source, int offset, int line, int column) {
        this.source = source;
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public int getLine() {
        return this.line;
    }

    @Override
    public int getColumn() {
        return this.column;
    }

    @Override
    public char[] getSource() {
        return this.source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Span span = (Span) o;

        if (this.offset != span.offset)
            return false;
        if (this.line != span.line)
            return false;
        return this.column == span.column;
    }

    @Override
    public int hashCode() {
        int result = this.offset;
        result = 31 * result + this.line;
        result = 31 * result + this.column;
        return result;
    }

    @Override
    public String toString() {
        return "Span(" + this.offset + ", " + this.line + ", " + this.column + ")";
    }
}
