package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.stream.EndOfStreamException;

import java.util.ArrayDeque;

public class TokenStream {

    private static final Token EOF = new Token(null, "", TokenType.EOF);

    private final ArrayDeque<Token> tokens;
    private final ISource source;

    public TokenStream(ISource source) {
        this(source, new ArrayDeque<>());
    }

    public TokenStream(ISource source, ArrayDeque<Token> tokens) {
        this.source = source;
        this.tokens = tokens;
    }

    void push(Token token) {
        this.tokens.addLast(token);
    }

    public TokenStream sanitized() {
        return new SanitizedTokenStream(this);
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

    public ArrayDeque<Token> getTokens() {
        return this.tokens;
    }

    private boolean isValidIndex(int offset) {
        return offset < this.tokens.size();
    }

    private void validateIndex(int offset) {
        if (!isValidIndex(offset))
            throw new EndOfStreamException();
    }

    private static class SanitizedTokenStream extends TokenStream {

        private final TokenStream delegate;

        private SanitizedTokenStream(TokenStream delegate) {
            super(delegate.getSource());
            this.delegate = delegate;
        }

        @Override
        public TokenStream sanitized() {
            return this;
        }

        @Override
        public Token consume() {
            var token = this.delegate.consume();
            if (shouldSkip(token.type())) {
                skip();
                return this.delegate.consume();
            }

            return token;
        }

        @Override
        public Token peek() {
            skip();
            return this.delegate.peek();
        }

        @Override
        public int tokensLeft() {
            skip();
            return this.delegate.tokensLeft();
        }

        @Override
        public boolean isEmpty() {
            skip();
            return this.delegate.isEmpty();
        }

        private void skip() {
            while (shouldSkip(this.delegate.peek().type()))
                this.delegate.consume();
        }

        private static boolean shouldSkip(TokenType type) {
            return type == TokenType.WHITESPACE || type == TokenType.COMMENT || type == TokenType.INVALID;
        }
    }
}
