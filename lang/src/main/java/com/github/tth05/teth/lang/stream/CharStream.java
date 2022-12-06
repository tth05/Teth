package com.github.tth05.teth.lang.stream;

import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.source.InMemorySource;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.BoundedIntStack;

public class CharStream {

    private final BoundedIntStack markedIndices = new BoundedIntStack(10);
    private final ISource source;
    private final char[] chars;

    private int index;


    private CharStream(ISource source) {
        this(source, source.getContents());
    }

    private CharStream(ISource source, char[] chars) {
        this.source = source;
        this.chars = chars;
    }

    public char consume() {
        if (!isValidIndex(0))
            throw new EndOfStreamException();

        var c = this.chars[this.index++];
        if (c == '\r')
            return consume();
        return c;
    }

    public Span consumeKnownSingle() {
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
        this.markedIndices.push(this.index);
    }

    public Span popMarkedSpan() {
        var markedIndex = this.markedIndices.pop();
        return new Span(this.source, markedIndex, this.index);
    }

    public Span createCurrentIndexSpan() {
        return new Span(this.source, this.index, this.index + 1);
    }

    public ISource getSource() {
        return this.source;
    }

    public static CharStream fromChars(char[] chars) {
        return new CharStream(new InMemorySource("tmp", chars), chars);
    }

    public static CharStream fromSource(ISource source) {
        return new CharStream(source);
    }
}
