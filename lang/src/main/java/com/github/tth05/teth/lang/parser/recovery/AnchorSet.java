package com.github.tth05.teth.lang.parser.recovery;

import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.*;

public class AnchorSet implements Iterable<TokenType> {

    private final AnchorSet parent;
    private final List<TokenType> anchorTokens;

    public AnchorSet(List<TokenType> anchorTokens) {
        this(null, Objects.requireNonNull(anchorTokens));
    }

    private AnchorSet(AnchorSet parent, List<TokenType> anchorTokens) {
        this.parent = parent;
        this.anchorTokens = anchorTokens;
    }

    public boolean contains(TokenType tokenType) {
        for (var anchorToken : this) {
            if (anchorToken == tokenType)
                return true;
        }

        return false;
    }

    public AnchorSet lazyUnion(AnchorSet parent) {
        return new AnchorSet(parent, this.anchorTokens);
    }

    @Override
    public Iterator<TokenType> iterator() {
        return new Iterator<>() {
            private AnchorSet current = AnchorSet.this;
            private int index;

            @Override
            public boolean hasNext() {
                return this.current != null && this.index < this.current.anchorTokens.size();
            }

            @Override
            public TokenType next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                var tokenType = this.current.anchorTokens.get(this.index++);
                if (this.index >= this.current.anchorTokens.size()) {
                    this.current = this.current.parent;
                    this.index = 0;
                }

                return tokenType;
            }
        };
    }

    public static AnchorSet union(AnchorSet... sets) {
        var size = 0;
        for (var set : sets)
            size += set.anchorTokens.size();

        var list = new ArrayList<TokenType>(size);
        for (var set : sets)
            list.addAll(set.anchorTokens);

        return new AnchorSet(list);
    }
}
