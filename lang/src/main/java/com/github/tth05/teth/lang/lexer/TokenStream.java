package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.parser.UnexpectedTokenException;
import com.github.tth05.teth.lang.stream.EndOfStreamException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TokenStream {

    private static final Token EOF = new Token("", TokenType.EOF);

    private final Deque<Token> tokens = new ArrayDeque<>();

    private int index;

    void push(Token token) {
        this.tokens.add(token);
    }

    public Token consumeType(TokenType expectedType) {
        return consumeTypeOrElse(expectedType, () -> {throw new UnexpectedTokenException(peek());});
    }

    public Token consumeTypeOrElse(TokenType expectedType, Runnable orElse) {
        if (!peek().is(expectedType)) {
            orElse.run();
            throw new IllegalStateException("TokenStream has no values");
        }

        return consume();
    }

    public Token consume() {
        validateIndex(0);

        return this.tokens.pop();
    }

    public Token peek() {
        return peek(0);
    }

    public Token peek(int offset) {
        if (!isValidIndex(offset))
            return EOF;

        return this.tokens.peek();
    }

    public List<Token> toList() {
        return List.of(this.tokens.toArray(new Token[0]));
    }

    public boolean isEmpty() {
        return this.tokens.isEmpty();
    }

    private boolean isValidIndex(int offset) {
        return offset < this.tokens.size();
    }

    private void validateIndex(int offset) {
        if (!isValidIndex(offset))
            throw new EndOfStreamException();
    }
}
