package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.stream.EndOfStreamException;

import java.util.ArrayDeque;

public class TokenStream {

    private static final Token EOF = new Token(null, "", TokenType.EOF);

    private final ArrayDeque<Token> tokens = new ArrayDeque<>();
    private final ISource source;

    public TokenStream(ISource source) {
        this.source = source;
    }

    void push(Token token) {
        this.tokens.addLast(token);
    }

    public Token consume() {
        validateIndex(0);

        return this.tokens.removeFirst();
    }

    public Token peek() {
        if (!isValidIndex(0))
            return EOF;

        return this.tokens.peekFirst();
    }

    public int tokensLeft() {
        return this.tokens.size();
    }

    public boolean isEmpty() {
        return this.tokens.isEmpty();
    }

    public ISource getSource() {
        return this.source;
    }

    private boolean isValidIndex(int offset) {
        return offset < this.tokens.size();
    }

    private void validateIndex(int offset) {
        if (!isValidIndex(offset))
            throw new EndOfStreamException();
    }
}
