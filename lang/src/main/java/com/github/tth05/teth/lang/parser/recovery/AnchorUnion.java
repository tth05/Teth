package com.github.tth05.teth.lang.parser.recovery;

import com.github.tth05.teth.lang.lexer.TokenType;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class AnchorUnion implements Iterable<TokenType> {

    private AnchorSet anchorSet;

    private AnchorUnion partA;
    private AnchorUnion partB;

    public AnchorUnion(AnchorSet a) {
        this.anchorSet = a;
    }

    public AnchorUnion(AnchorUnion a, AnchorUnion b) {
        this.partA = a;
        this.partB = b;
    }

    public boolean contains(TokenType tokenType) {
        for (var anchorToken : this) {
            if (anchorToken == tokenType)
                return true;
        }

        return false;
    }


    public AnchorUnion union(AnchorUnion other) {
        return new AnchorUnion(this, other);
    }

    @Override
    public Iterator<TokenType> iterator() {
        return new Iterator<>() {

            private final boolean isLeaf = AnchorUnion.this.anchorSet != null;

            private Iterator<TokenType> iterator = this.isLeaf ? AnchorUnion.this.anchorSet.iterator() : AnchorUnion.this.partA.iterator();
            private boolean isPartA = true;

            @Override
            public boolean hasNext() {
                if (this.isLeaf)
                    return this.iterator.hasNext();

                var res = this.iterator.hasNext();
                if (!res && this.isPartA) {
                    this.iterator = AnchorUnion.this.partB.iterator();
                    this.isPartA = false;
                    res = this.iterator.hasNext();
                }

                return res;
            }

            @Override
            public TokenType next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return this.iterator.next();
            }
        };
    }

    public static AnchorUnion leaf(List<TokenType> tokenTypes) {
        return new AnchorUnion(new AnchorSet(tokenTypes));
    }
}
