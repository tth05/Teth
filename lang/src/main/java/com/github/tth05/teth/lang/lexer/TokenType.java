package com.github.tth05.teth.lang.lexer;

public enum TokenType {

    IDENTIFIER,
    KEYWORD,
    LONG_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,
    BOOLEAN_LITERAL,
    COMMA,
    DOT,
    EQUAL,
    NOT,
    EQUAL_EQUAL,
    NOT_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    POW,
    L_PAREN,
    R_PAREN,
    L_CURLY_PAREN,
    R_CURLY_PAREN,
    LINE_BREAK,
    EOF;
}
