package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.source.InMemorySource;
import com.github.tth05.teth.lang.util.CharArrayUtils;

import java.util.Arrays;
import java.util.List;

public record Span(ISource source, int offset, int offsetEnd) implements CharSequence {

    public int getStartLine() {
        if (source() == null)
            return -1;

        return CharArrayUtils.getLineNumber(source().getContents(), offset());
    }

    public int getEndLine() {
        if (source() == null)
            return -1;

        return CharArrayUtils.getLineNumber(source().getContents(), offsetEnd());
    }

    public int getStartColumn() {
        if (source() == null)
            return -1;

        return offset() - CharArrayUtils.getLineStart(source().getContents(), offset());
    }

    /**
     * @return The exclusive end column of the span.
     */
    public int getEndColumn() {
        if (source() == null)
            return -1;

        return offsetEnd() - CharArrayUtils.getLineStart(source().getContents(), offsetEnd());
    }

    @Override
    public int length() {
        return offsetEnd() - offset();
    }

    @Override
    public char charAt(int index) {
        return source().getContents()[offset() + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }

    public String getText() {
        return new String(source().getContents(), offset(), length());
    }

    public boolean textEquals(String text) {
        if (text.length() != length())
            return false;

        var contents = source().getContents();
        for (int i = 0; i < text.length(); i++) {
            if (contents[offset() + i] != text.charAt(i))
                return false;
        }

        return true;
    }

    public boolean textEquals(Span span) {
        if (span.length() != length())
            return false;

        var contents = source().getContents();
        var otherContents = span.source().getContents();
        var invalid = offset() >= contents.length;
        var otherInvalid = span.offset() >= otherContents.length;
        if (invalid && otherInvalid)
            return true;
        if (invalid || otherInvalid)
            return false;
        return Arrays.equals(contents, offset(), offsetEnd(), otherContents, span.offset(), span.offsetEnd());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Span span))
            return false;

        return textEquals(span);
    }

    public boolean spanEquals(Span other) {
        if (this == other)
            return true;

        if (this.offset != other.offset)
            return false;
        if (this.offsetEnd != other.offsetEnd)
            return false;
        return this.source == other.source;
    }

    @Override
    public int hashCode() {
        var ar = source().getContents();
        var result = 1;
        for (int i = offset(); i < offsetEnd(); i++)
            result = 31 * result + ar[i];

        return result;
    }

    public int spanHashcode() {
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
        if (first.spanEquals(last))
            return first;

        return new Span(first.source(), first.offset(), last.offsetEnd());
    }

    public static <T extends Statement> Span of(List<T> list, Span fallback) {
        if (list.size() > 0) {
            var a = list.get(0).getSpan();
            var b = list.get(list.size() - 1).getSpan();
            if (a == null || b == null)
                return fallback;

            return Span.of(a, b);
        } else {
            return fallback;
        }
    }

    @Deprecated
    public static Span fromString(String text) {
        return new Span(new InMemorySource("tmp", text), 0, text.length());
    }
}
