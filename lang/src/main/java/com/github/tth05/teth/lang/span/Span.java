package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.Arrays;
import java.util.List;

public record Span(char[] source, int offset, int offsetEnd) implements ISpan {

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
        return "Span(" + this.offset + ", " + this.offsetEnd + ", " + getStartLine() + ", " + getStartColumn() + ")";
    }

    public static Span of(ISpan first, ISpan last) {
        if (first.source() != last.source())
            throw new IllegalArgumentException("Spans must be from the same source");

        return new Span(first.source(), first.offset(), last.offsetEnd());
    }

    public static <T extends Statement> ISpan of(List<T> list, ISpan fallback) {
        if (list.size() > 0) {
            return Span.of(list.get(0).getSpan(), list.get(list.size() - 1).getSpan());
        } else {
            return fallback;
        }
    }
}
