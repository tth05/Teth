package com.github.tth05.teth.lang.lexer;

public class UnexpectedCharException extends RuntimeException {

    public UnexpectedCharException(char c) {
        super("Unexpected character '" + c + "'");
    }

    public UnexpectedCharException(char c, TokenType currentType) {
        super("Unexpected character '" + c + "' while tokenizing " + currentType);
    }
}
