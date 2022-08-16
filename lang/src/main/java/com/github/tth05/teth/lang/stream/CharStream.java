package com.github.tth05.teth.lang.stream;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.Span;

public class CharStream {

    private final char[] chars;

    private int markedIndex = -1;

    private int index;

    private CharStream(String source) {
        this.chars = source.trim().toCharArray();
    }

    public char consume() {
        if (!isValidIndex(0))
            throw new EndOfStreamException();

        var c = this.chars[this.index++];
        if (c == '\r')
            return consume();
        return c;
    }

    public ISpan consumeKnownSingle() {
        var span = createCurrentIndexSpan();
        consume();
        return span;
    }

    public char peek() {
        return peek(0);
    }

    public char peek(int offset) {
        if (!isValidIndex(offset))
            return 0;

        var c = this.chars[this.index + offset];
        if (c == '\r') {
            this.index++;
            return peek(offset);
        }

        return c;
    }

    public boolean isEmpty() {
        return peek() == 0;
    }

    private boolean isValidIndex(int offset) {
        return this.index + offset < this.chars.length;
    }

    public void markSpan() {
        this.markedIndex = this.index;
    }

    public ISpan createMarkedSpan() {
        if (this.markedIndex == -1)
            throw new IllegalStateException("No mark set");

        var span = new Span(this.chars, this.markedIndex, this.index);
        this.markedIndex = -1;
        return span;
    }

    public ISpan createCurrentIndexSpan() {
        return new Span(this.chars, this.index, this.index + 1);
    }

    public static CharStream fromString(String source) {
        return new CharStream(source);
    }
}
