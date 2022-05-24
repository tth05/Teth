package com.github.tth05.teth.lang.span;

public interface ISpan {

    int getOffset();

    /**
     * @return The exclusive end offset of the span.
     */
    int getOffsetEnd();

    int getLine();

    int getColumn();

    /**
     * @return The exclusive end column of the span.
     */
    default int getColumnEnd() {
        return getColumn() + getOffsetEnd() - getOffset();
    }

    char[] getSource();
}
