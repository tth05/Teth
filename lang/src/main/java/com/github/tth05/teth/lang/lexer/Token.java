package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.span.ISpan;

public record Token(ISpan span, String value, TokenType type) {

    public static final Token INVALID = new Token(null, null, null);

    public boolean is(TokenType type) {
        return this.type == type;
    }

    public boolean isInvalid() {
        return this == INVALID;
    }
}
