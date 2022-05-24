package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class UnexpectedTokenException extends SpanAwareException {

    public UnexpectedTokenException(ISpan span, String fmt, Object... args) {
        super(span, String.format(fmt, args));
    }
}
