package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class ValidationException extends SpanAwareException {

    public ValidationException(ISpan span, String message) {
        super(span, message);
    }
}
