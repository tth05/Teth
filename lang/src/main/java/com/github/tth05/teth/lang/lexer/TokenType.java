package com.github.tth05.teth.lang.lexer;

public enum TokenType {

    IDENTIFIER("Identifier"),
    KEYWORD("Keywod"),
    LONG_LITERAL("Long"),
    DOUBLE_LITERAL("Double"),
    STRING_LITERAL("String"),
    STRING_LITERAL_CODE_START("{"),
    STRING_LITERAL_CODE_END("}"),
    BOOLEAN_LITERAL("Boolean"),
    COMMA(","),
    DOT("."),
    COLON(":"),
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
    SLASH("/"),
    POW("^"),
    AMPERSAND_AMPERSAND("&&"),
    PIPE_PIPE("||"),
    LESS_PIPE("<|"),
    L_PAREN("("),
    R_PAREN(")"),
    L_SQUARE_BRACKET("["),
    R_SQUARE_BRACKET("]"),
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
