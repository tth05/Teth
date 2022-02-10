package com.github.tth05.teth.lang.lexer;

public record Token(String value, TokenType type) {

    public boolean is(TokenType type) {
        return this.type == type;
    }

    public boolean isLineTerminating() {
        return this.type == TokenType.EOF || this.type == TokenType.LINE_BREAK;
    }
}
