package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.diagnostics.Problem;

public class SpanAwareException extends RuntimeException {

    private final Span span;

    public SpanAwareException(Span span, String message) {
        super(message);
        this.span = span;
    }

    public Span getSpan() {
        return this.span;
    }

    public Problem asProblem() {
        return new Problem(this.span, this.getMessage());
    }
}
