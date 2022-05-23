package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.SpanAwareException;

public class UnexpectedCharException extends SpanAwareException {

    public UnexpectedCharException(ISpan span, char c) {
        super(span, "Unexpected character '" + c + "'");
    }

    public UnexpectedCharException(ISpan span, char c, TokenType currentType) {
        super(span, "Unexpected character '" + c + "' while tokenizing " + currentType);
    }
}
