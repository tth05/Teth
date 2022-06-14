package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class TypeResolverException extends SpanAwareException {

    public TypeResolverException(ISpan span, String message) {
        super(span, message);
    }
}
