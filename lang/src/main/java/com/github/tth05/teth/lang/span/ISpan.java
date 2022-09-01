package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.util.CharArrayUtils;

public interface ISpan {

    int offset();

    /**
     * @return The exclusive end offset of the span.
     */
    int offsetEnd();

    ISource source();

    default int getStartLine() {
        return CharArrayUtils.getLineNumber(source().getContents(), offset());
    }

    default int getEndLine() {
        return CharArrayUtils.getLineNumber(source().getContents(), offsetEnd());
    }

    default int getStartColumn() {
        return offset() - CharArrayUtils.getLineStart(source().getContents(), offset());
    }

    /**
     * @return The exclusive end column of the span.
     */
    default int getEndColumn() {
        return offsetEnd() - CharArrayUtils.getLineStart(source().getContents(), offsetEnd());
    }
}
