package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.util.CharArrayUtils;

import java.util.List;

public record Span(ISource source, int offset, int offsetEnd) {

    public int getStartLine() {
        return CharArrayUtils.getLineNumber(source().getContents(), offset());
    }

    public int getEndLine() {
        return CharArrayUtils.getLineNumber(source().getContents(), offsetEnd());
    }

    public int getStartColumn() {
        return offset() - CharArrayUtils.getLineStart(source().getContents(), offset());
    }

    /**
     * @return The exclusive end column of the span.
     */
    public int getEndColumn() {
        return offsetEnd() - CharArrayUtils.getLineStart(source().getContents(), offsetEnd());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Span span))
            return false;

        if (this.offset != span.offset)
            return false;
        if (this.offsetEnd != span.offsetEnd)
            return false;
        return this.source == span.source;
    }

    @Override
    public int hashCode() {
        int result = this.source != null ? this.source.hashCode() : 0;
        result = 31 * result + this.offset;
        result = 31 * result + this.offsetEnd;
        return result;
    }

    @Override
    public String toString() {
        return "Span(" + this.offset + ", " + this.offsetEnd + ", " + getStartLine() + ", " + getStartColumn() + ")";
    }

    public static Span of(Span first, Span last) {
        if (first.source() != last.source())
            throw new IllegalArgumentException("Spans must be from the same source");
        if (first.equals(last))
            return first;

        return new Span(first.source(), first.offset(), last.offsetEnd());
    }

    public static <T extends Statement> Span of(List<T> list, Span fallback) {
        if (list.size() > 0) {
            return Span.of(list.get(0).getSpan(), list.get(list.size() - 1).getSpan());
        } else {
            return fallback;
        }
    }
}
