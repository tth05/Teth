package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.parser.UnexpectedTokenException;
import com.github.tth05.teth.lang.stream.EndOfStreamException;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class TokenStream {

    private static final Token EOF = new Token(null, "", TokenType.EOF);

    private final LinkedList<Token> tokens = new LinkedList<>();

    private int index;

    void push(Token token) {
        this.tokens.add(token);
    }

    public Token consumeMatchingOrElse(Predicate<Token> predicate, Runnable orElse) {
        if (!predicate.test(peek())) {
            orElse.run();
            throw new IllegalStateException("TokenStream has no values");
        }

        return consume();
    }

    public Token consumeType(TokenType expectedType) {
        return consumeTypeOrElse(expectedType, () -> {
            throw new UnexpectedTokenException(peek().span(), "Expected token '%s'", expectedType.getText());
        });
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

        return this.tokens.removeFirst();
    }

    public Token peek() {
        if (!isValidIndex(0))
            return EOF;

        return this.tokens.peekFirst();
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
