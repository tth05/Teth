package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.Arrays;

public class UnexpectedTokenException extends RuntimeException {

    public UnexpectedTokenException(Token token) {
        super("Unexpected token '" + token + "'");
    }

    public UnexpectedTokenException(Token token, TokenType... expectedTokens) {
        super("Unexpected token '" + token + "', expected one of " + Arrays.toString(expectedTokens));
    }
}
