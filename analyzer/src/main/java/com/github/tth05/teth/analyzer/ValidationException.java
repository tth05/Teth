package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class ValidationException extends SpanAwareException {

    public ValidationException(Span span, String message) {
        super(span, message);
    }
}
