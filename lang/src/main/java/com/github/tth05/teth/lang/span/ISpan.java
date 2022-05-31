package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.util.CharArrayUtils;

public interface ISpan {

    int offset();

    /**
     * @return The exclusive end offset of the span.
     */
    int offsetEnd();

    char[] source();

    default int getStartLine() {
        return CharArrayUtils.getLineNumber(source(), offset());
    }

    default int getEndLine() {
        return CharArrayUtils.getLineNumber(source(), offsetEnd());
    }

    default int getStartColumn() {
        return offset() - CharArrayUtils.getLineStart(source(), offset());
    }

    /**
     * @return The exclusive end column of the span.
     */
    default int getEndColumn() {
        return offsetEnd() - CharArrayUtils.getLineStart(source(), offsetEnd());
    }
}
