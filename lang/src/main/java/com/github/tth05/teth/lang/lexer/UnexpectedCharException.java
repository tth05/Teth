package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class UnexpectedCharException extends SpanAwareException {

    public UnexpectedCharException(ISpan span, String fmt, Object... args) {
        super(span, String.format(fmt, args));
    }
}
