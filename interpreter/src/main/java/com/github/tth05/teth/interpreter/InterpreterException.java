package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class InterpreterException extends SpanAwareException {

    public InterpreterException(ISpan span, String message) {
        super(span, message);
    }
}
