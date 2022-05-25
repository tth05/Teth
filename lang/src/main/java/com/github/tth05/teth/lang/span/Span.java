package com.github.tth05.teth.lang.span;

import java.util.Arrays;

public final class Span implements ISpan {

    private final char[] source;
    private final int offset;
    private final int offsetEnd;

    public Span(char[] source, int offset, int offsetEnd) {
        this.source = source;
        this.offset = offset;
        this.offsetEnd = offsetEnd;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public int getOffsetEnd() {
        return this.offsetEnd;
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
        if (this.offsetEnd != span.offsetEnd)
            return false;
        return Arrays.equals(this.source, span.source);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.source);
        result = 31 * result + this.offset;
        result = 31 * result + this.offsetEnd;
        return result;
    }

    @Override
    public String toString() {
        return "Span(" + this.offset  + ", " + this.offsetEnd + ", " + getStartLine() + ", " + getStartColumn() + ")";
    }

    public static Span of(ISpan first, ISpan last) {
        return new Span(first.getSource(), first.getOffset(), last.getOffsetEnd());
    }
}
