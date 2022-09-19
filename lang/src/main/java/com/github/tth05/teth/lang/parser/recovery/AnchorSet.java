package com.github.tth05.teth.lang.parser.recovery;

import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.*;

public class AnchorSet implements Iterable<TokenType> {

    private final List<TokenType> anchorTokens;

    public AnchorSet(List<TokenType> anchorTokens) {
        this.anchorTokens = Objects.requireNonNull(anchorTokens);
    }

    @Override
    public Iterator<TokenType> iterator() {
        return this.anchorTokens.iterator();
    }
}
