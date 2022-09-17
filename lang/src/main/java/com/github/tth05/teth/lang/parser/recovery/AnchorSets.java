package com.github.tth05.teth.lang.parser.recovery;

import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.Collections;
import java.util.List;

public class AnchorSets {

    public static final AnchorSet EMPTY = new AnchorSet(Collections.emptyList());

    public static final AnchorSet FIRST_SET_ELSE_STATEMENT = new AnchorSet(List.of(TokenType.KEYWORD_ELSE));
    public static final AnchorSet FIRST_SET_BLOCK = new AnchorSet(List.of(TokenType.L_CURLY_PAREN));
    public static final AnchorSet END_SET_BLOCK = new AnchorSet(List.of(TokenType.R_CURLY_PAREN));
    //TODO: Include expression statement
    public static final AnchorSet FIRST_SET_STATEMENT = AnchorSet.union(FIRST_SET_BLOCK, new AnchorSet(List.of(
            TokenType.KEYWORD_IF, TokenType.KEYWORD_ELSE, TokenType.KEYWORD_LET, TokenType.KEYWORD_RETURN,
            TokenType.KEYWORD_LOOP, TokenType.KEYWORD_NEW, TokenType.KEYWORD_FN, TokenType.KEYWORD_STRUCT,
            TokenType.KEYWORD_USE
    )));
    public static final AnchorSet FIRST_SET_BLOCK_STATEMENT = AnchorSet.union(FIRST_SET_BLOCK, FIRST_SET_STATEMENT);
    public static final AnchorSet FIRST_SET_USE_STATEMENT = AnchorSet.union(new AnchorSet(List.of(TokenType.SLASH)), FIRST_SET_BLOCK);
    public static final AnchorSet END_SET_USE_STATEMENT = AnchorSet.union(new AnchorSet(List.of(TokenType.COMMA)), END_SET_BLOCK);
}
