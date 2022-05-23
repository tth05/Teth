package com.github.tth05.teth.lang.span;

public final class MutableSpan implements ISpan {

    private final char[] source;

    private int offset;
    private int line;
    private int column;

    public MutableSpan(char[] source) {
        this.source = source;
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

    public void advanceColumn() {
        this.column++;
        this.offset++;
    }

    public void advanceLine() {
        this.line++;
        this.column = 0;
        this.offset++;
    }

    public ISpan toImmutable() {
        return new Span(this.source, this.offset, this.line, this.column);
    }
}
