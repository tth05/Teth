package com.github.tth05.tethintellijplugin.syntax;

import com.github.tth05.teth.lang.lexer.TokenType;
import com.intellij.psi.tree.IElementType;

public class TethTokenTypes {

    public static final TethTokenType KEYWORD = new TethTokenType("Keyword");
    public static final TethTokenType IDENTIFIER = new TethTokenType("Identifier");
    public static final TethTokenType SEPARATOR = new TethTokenType("Separator");
    public static final TethTokenType STRING_LITERAL = new TethTokenType("String literal");
    public static final TethTokenType DOUBLE = new TethTokenType("Double literal");
    public static final TethTokenType LONG = new TethTokenType("Long literal");
    public static final TethTokenType COMMENT = new TethTokenType("Comment");

    public static IElementType of(TokenType type) {
        return switch (type) {
            case IDENTIFIER -> IDENTIFIER;
            case STRING_LITERAL -> STRING_LITERAL;
            case DOUBLE_LITERAL -> DOUBLE;
            case LONG_LITERAL -> LONG;
            case BOOLEAN_LITERAL -> KEYWORD;
            case KEYWORD_IF, KEYWORD_ELSE, KEYWORD_FN, KEYWORD_LET, KEYWORD_LOOP, KEYWORD_STRUCT, KEYWORD_NEW,
                    KEYWORD_RETURN, KEYWORD_USE -> KEYWORD;
            case AMPERSAND_AMPERSAND, COLON, COMMA, L_CURLY_PAREN, R_CURLY_PAREN, L_PAREN, R_PAREN, DOT, EQUAL,
                    GREATER_EQUAL, GREATER, LESS, LESS_EQUAL, LESS_PIPE, EQUAL_EQUAL, PIPE_PIPE, NOT_EQUAL, MINUS,
                    PLUS, MULTIPLY, SLASH, POW, NOT, L_SQUARE_BRACKET, R_SQUARE_BRACKET, STRING_LITERAL_CODE_END,
                    STRING_LITERAL_CODE_START -> SEPARATOR;
            case COMMENT -> COMMENT;
            case WHITESPACE, LINE_BREAK -> com.intellij.psi.TokenType.WHITE_SPACE;
            case INVALID -> com.intellij.psi.TokenType.BAD_CHARACTER;
            default -> throw new IllegalArgumentException("Unknown token type: " + type);
        };
    }
}
