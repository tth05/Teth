package com.github.tth05.teth.lang.lexer;

public enum TokenType {

    IDENTIFIER("Identifier"),
    KEYWORD("Keywod"),
    LONG_LITERAL("Long"),
    DOUBLE_LITERAL("Double"),
    STRING_LITERAL("String"),
    BOOLEAN_LITERAL("Boolean"),
    COMMA(","),
    DOT("."),
    EQUAL("="),
    NOT("!"),
    EQUAL_EQUAL("=="),
    NOT_EQUAL("!="),
    LESS("<"),
    LESS_EQUAL("<="),
    GREATER(">"),
    GREATER_EQUAL(">="),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    POW("^"),
    L_PAREN("("),
    R_PAREN(")"),
    L_CURLY_PAREN("{"),
    R_CURLY_PAREN("}"),
    LINE_BREAK("Line break"),
    EOF("End of file");

    private final String text;

    TokenType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
