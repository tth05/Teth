package com.github.tth05.teth.lang.span;

public class SpanAwareException extends RuntimeException {

    private final ISpan span;

    public SpanAwareException(ISpan span, String message) {
        super(message);
        this.span = span;
    }

    public ISpan getSpan() {
        return this.span;
    }
}
