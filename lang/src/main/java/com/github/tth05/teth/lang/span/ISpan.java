package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.util.CharArrayUtils;

public interface ISpan {

    int getOffset();

    /**
     * @return The exclusive end offset of the span.
     */
    int getOffsetEnd();

    char[] getSource();

    default int getStartLine() {
        return CharArrayUtils.getLineNumber(getSource(), getOffset());
    }

    default int getEndLine() {
        return CharArrayUtils.getLineNumber(getSource(), getOffsetEnd());
    }

    default int getStartColumn() {
        return getOffset() - CharArrayUtils.getLineStart(getSource(), getOffset());
    }

    /**
     * @return The exclusive end column of the span.
     */
    default int getEndColumn() {
        return getOffsetEnd() - CharArrayUtils.getLineStart(getSource(), getOffsetEnd());
    }
}
