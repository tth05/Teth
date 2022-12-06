package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.span.Span;

public record Token(Span span, TokenType type) {

    public static final Token INVALID = new Token(null, null);

    public boolean is(TokenType type) {
        return this.type == type;
    }

    public boolean isInvalid() {
        return this == INVALID;
    }

    public String text() {
        return this.span.getText();
    }

    public boolean textEquals(String text) {
        if (this.span == null)
            return false;

        return this.span.textEquals(text);
    }
}
