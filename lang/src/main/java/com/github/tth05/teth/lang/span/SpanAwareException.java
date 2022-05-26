package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.diagnostics.Problem;

public class SpanAwareException extends RuntimeException {

    private final ISpan span;

    public SpanAwareException(ISpan span, String message) {
        super(message);
        this.span = span;
    }

    public ISpan getSpan() {
        return this.span;
    }

    public Problem asProblem() {
        return new Problem(this.span, this.getMessage());
    }
}
