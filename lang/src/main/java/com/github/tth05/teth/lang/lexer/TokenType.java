package com.github.tth05.teth.lang.lexer;

public enum TokenType {

    IDENTIFIER("Identifier"),
    KEYWORD_IF("if"),
    KEYWORD_ELSE("else"),
    KEYWORD_FN("fn"),
    KEYWORD_RETURN("return"),
    KEYWORD_LET("let"),
    KEYWORD_LOOP("loop"),
    KEYWORD_BREAK("break"),
    KEYWORD_CONTINUE("continue"),
    KEYWORD_NEW("new"),
    KEYWORD_STRUCT("struct"),
    KEYWORD_USE("use"),
    KEYWORD_NULL("null"),
    KEYWORD_INTRINSIC("intrinsic"),
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
    COMMENT("Comment"),
    WHITESPACE("Whitespace"),
    LINE_BREAK("Line break"),
    INVALID("Invalid"),
    EOF("End of file");

    private final String text;

    TokenType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
