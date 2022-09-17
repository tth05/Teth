package com.github.tth05.teth.lang.parser.recovery;

import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.Collections;
import java.util.List;

public class AnchorSets {

    public static final AnchorSet EMPTY = new AnchorSet(Collections.emptyList());

    public static final AnchorSet ALL_EXPRESSION_OPERATORS = new AnchorSet(List.of(
            TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL,
            TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.SLASH, TokenType.POW,
            TokenType.DOT, TokenType.NOT, TokenType.EQUAL, TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL,
            TokenType.AMPERSAND_AMPERSAND, TokenType.PIPE_PIPE
    ));
    public static final AnchorSet FIRST_SET_PRIMARY_EXPRESSION = new AnchorSet(List.of(
            TokenType.IDENTIFIER, TokenType.LONG_LITERAL, TokenType.DOUBLE_LITERAL, TokenType.STRING_LITERAL,
            TokenType.BOOLEAN_LITERAL, TokenType.L_PAREN, TokenType.KEYWORD_NEW
    ));
    public static final AnchorSet FIRST_SET_UNARY_EXPRESSION = new AnchorSet(List.of(
            TokenType.MINUS, TokenType.NOT
    ));
    public static final AnchorSet FIRST_SET_POSTFIX_OP = new AnchorSet(List.of(
            TokenType.DOT, TokenType.L_PAREN, TokenType.LESS_PIPE
    ));
    public static final AnchorSet FIRST_SET_EXPRESSION = AnchorSet.union(FIRST_SET_PRIMARY_EXPRESSION, FIRST_SET_UNARY_EXPRESSION, FIRST_SET_POSTFIX_OP);
    public static final AnchorSet FIRST_SET_ELSE_STATEMENT = new AnchorSet(List.of(TokenType.KEYWORD_ELSE));
    public static final AnchorSet FIRST_SET_BLOCK = new AnchorSet(List.of(TokenType.L_CURLY_PAREN));
    public static final AnchorSet END_SET_BLOCK = new AnchorSet(List.of(TokenType.R_CURLY_PAREN));
    public static final AnchorSet FIRST_SET_KEYWORD_STATEMENT = new AnchorSet(List.of(
            TokenType.KEYWORD_IF, TokenType.KEYWORD_ELSE, TokenType.KEYWORD_LET, TokenType.KEYWORD_RETURN,
            TokenType.KEYWORD_LOOP, TokenType.KEYWORD_NEW, TokenType.KEYWORD_FN, TokenType.KEYWORD_STRUCT,
            TokenType.KEYWORD_USE
    ));
    public static final AnchorSet FIRST_SET_STATEMENT = AnchorSet.union(FIRST_SET_BLOCK, FIRST_SET_KEYWORD_STATEMENT, FIRST_SET_EXPRESSION);
    public static final AnchorSet FIRST_SET_USE_STATEMENT = AnchorSet.union(new AnchorSet(List.of(TokenType.SLASH)), FIRST_SET_BLOCK);
    public static final AnchorSet END_SET_USE_STATEMENT = AnchorSet.union(new AnchorSet(List.of(TokenType.COMMA)), END_SET_BLOCK);
}
