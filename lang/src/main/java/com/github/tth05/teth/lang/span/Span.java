package com.github.tth05.teth.lang.span;

public class Span implements ISpan {

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
}
