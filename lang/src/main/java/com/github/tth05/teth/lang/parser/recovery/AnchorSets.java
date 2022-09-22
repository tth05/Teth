package com.github.tth05.teth.lang.parser.recovery;

import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.Collections;
import java.util.List;

public class AnchorSets {

    public static final AnchorUnion EMPTY = AnchorUnion.leaf(Collections.emptyList());

    public static final AnchorUnion BINARY_EXPRESSION_OPERATORS = AnchorUnion.leaf(List.of(
            TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL,
            TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.SLASH, TokenType.POW,
            TokenType.EQUAL, TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL, TokenType.AMPERSAND_AMPERSAND,
            TokenType.PIPE_PIPE
    ));
    public static final AnchorUnion FIRST_SET_PRIMARY_EXPRESSION =AnchorUnion.leaf(List.of(
            TokenType.IDENTIFIER, TokenType.LONG_LITERAL, TokenType.DOUBLE_LITERAL, TokenType.STRING_LITERAL,
            TokenType.BOOLEAN_LITERAL, TokenType.L_PAREN, TokenType.KEYWORD_NEW
    ));
    public static final AnchorUnion FIRST_SET_UNARY_EXPRESSION = AnchorUnion.leaf(List.of(
            TokenType.MINUS, TokenType.NOT
    ));
    public static final AnchorUnion FIRST_SET_POSTFIX_OP = AnchorUnion.leaf(List.of(
            TokenType.DOT, TokenType.L_PAREN, TokenType.LESS_PIPE
    ));
    public static final AnchorUnion FIRST_SET_ELSE_STATEMENT = AnchorUnion.leaf(List.of(TokenType.KEYWORD_ELSE));
    public static final AnchorUnion FIRST_SET_BLOCK = AnchorUnion.leaf(List.of(TokenType.L_CURLY_PAREN));
    public static final AnchorUnion END_SET_BLOCK =  AnchorUnion.leaf(List.of(TokenType.R_CURLY_PAREN));
    public static final AnchorUnion FIRST_SET_PARENTHESISED_EXPRESSION =  AnchorUnion.leaf(List.of(TokenType.L_PAREN));
    public static final AnchorUnion END_SET_PARENTHESISED_EXPRESSION =  AnchorUnion.leaf(List.of(TokenType.R_PAREN));
    public static final AnchorUnion FIRST_SET_EXPRESSION = FIRST_SET_PRIMARY_EXPRESSION.union(FIRST_SET_UNARY_EXPRESSION).union(FIRST_SET_PARENTHESISED_EXPRESSION);
    public static final AnchorUnion FIRST_SET_KEYWORD_STATEMENT = AnchorUnion.leaf(List.of(
            TokenType.KEYWORD_IF, TokenType.KEYWORD_ELSE, TokenType.KEYWORD_LET, TokenType.KEYWORD_RETURN,
            TokenType.KEYWORD_LOOP, TokenType.KEYWORD_NEW, TokenType.KEYWORD_FN, TokenType.KEYWORD_STRUCT,
            TokenType.KEYWORD_USE
    ));
    public static final AnchorUnion FIRST_SET_STATEMENT = FIRST_SET_BLOCK.union(FIRST_SET_KEYWORD_STATEMENT).union(FIRST_SET_EXPRESSION);
    public static final AnchorUnion FIRST_SET_STATEMENT_EXPRESSIONLESS = FIRST_SET_BLOCK.union(FIRST_SET_KEYWORD_STATEMENT);

    public static final AnchorUnion FIRST_SET_USE_STATEMENT = FIRST_SET_BLOCK.union(AnchorUnion.leaf(List.of(TokenType.SLASH)));
    public static final AnchorUnion FIRST_SET_LIST = AnchorUnion.leaf(List.of(TokenType.COMMA));
    public static final AnchorUnion END_SET_USE_STATEMENT = END_SET_BLOCK.union(FIRST_SET_LIST);

    public static final AnchorUnion FIRST_SET_LET_STATEMENT = FIRST_SET_EXPRESSION.union(AnchorUnion.leaf(List.of(TokenType.COLON, TokenType.EQUAL)));
    public static final AnchorUnion MIDDLE_SET_LET_STATEMENT =  FIRST_SET_EXPRESSION.union(AnchorUnion.leaf(List.of(TokenType.EQUAL)));

    public static final AnchorUnion FIRST_SET_FUNCTION = FIRST_SET_EXPRESSION.union(AnchorUnion.leaf(List.of(TokenType.L_PAREN, TokenType.LESS)));
}
