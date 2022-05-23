package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.span.ISpan;

public record Token(ISpan span, String value, TokenType type) {

    public boolean is(TokenType type) {
        return this.type == type;
    }
}
