package com.github.tth05.teth.lang.stream;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.MutableSpan;

public class CharStream {

    private final MutableSpan span;
    private final char[] chars;

    private int index;

    private CharStream(String source) {
        this.chars = source.toCharArray();
        this.span = new MutableSpan(this.chars);
    }

    public char consume() {
        if (!isValidIndex(0))
            throw new EndOfStreamException();

        var c = this.chars[this.index++];
        if (c == '\n')
            this.span.advanceLine();
        else
            this.span.advanceColumn();
        return c;
    }

    public char peek() {
        return peek(0);
    }

    public char peek(int offset) {
        if (!isValidIndex(offset))
            return 0;

        return this.chars[this.index + offset];
    }

    public boolean isEmpty() {
        return peek() == 0;
    }

    private boolean isValidIndex(int offset) {
        return this.index + offset < this.chars.length;
    }

    public ISpan getSpan() {
        return this.span.toImmutable();
    }

    public static CharStream fromString(String source) {
        return new CharStream(source + '\u0000');
    }
}
