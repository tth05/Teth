package com.github.tth05.teth.lang.span;

public final class MarkerSpan implements ISpan {

    private final char[] source;

    private int offset;
    private int offsetEnd;
    private int line;
    private int column;

    private boolean marked;

    public MarkerSpan(char[] source) {
        this.source = source;
    }

    public void mark() {
        this.marked = true;
        this.offsetEnd = this.offset;
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

    public void advance() {
        if (!this.marked) {
            this.column++;
            this.offset++;
        } else {
            this.offsetEnd++;
        }
    }

    public void advanceLine() {
        if (this.marked)
            throw new IllegalStateException("Cannot advance line while marked");

        this.line++;
        this.column = 0;
        this.offset++;
    }

    public ISpan createMarkedSpan() {
        if (!this.marked)
            throw new IllegalStateException("Not marked");

        this.marked = false;
        var span = new Span(this.source, this.offset, this.offsetEnd, this.line, this.column);
        this.column += this.offsetEnd - this.offset;
        this.offset = this.offsetEnd;
        return span;
    }
}
