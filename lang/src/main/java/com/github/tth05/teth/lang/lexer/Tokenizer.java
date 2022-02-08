package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final CharStream stream;

    public Tokenizer(CharStream stream) {
        this.stream = stream;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            char c = this.stream.peek();
            if (c == 0) {
                tokens.add(new Token("", TokenType.EOF));
                break;
            }

            if (isNumber(c)) {
                tokens.add(parseNumber());
            } else if (isIdentifierChar(c)) {
                tokens.add(parseIdentifier());
            } else if (isOperator(c)) {
                tokens.add(parseOperator());
            } else if (isLineBreak(c)) {
                this.stream.consume();
                tokens.add(new Token("\n", TokenType.LINE_BREAK));
            } else if (isWhitespace(c)) {
                this.stream.consume();
            } else {
                throw new UnexpectedCharException(c);
            }
        }

        return tokens;
    }

    private Token parseIdentifier() {
        StringBuilder ident = new StringBuilder(8);
        do {
            char c = this.stream.consume();
            if (!isIdentifierChar(c))
                throw new UnexpectedCharException(c, TokenType.IDENTIFIER);

            ident.append(c);
        } while (!isSeparator(this.stream.peek()));

        return new Token(ident.toString(), TokenType.IDENTIFIER);
    }

    private Token parseNumber() {
        StringBuilder number = new StringBuilder(2);
        do {
            char c = this.stream.consume();
            if (!isNumber(c))
                throw new UnexpectedCharException(c, TokenType.NUMBER);

            number.append(c);
        } while (!isSeparator(this.stream.peek()));

        return new Token(number.toString(), TokenType.NUMBER);
    }

    private Token parseOperator() {
        var op = this.stream.consume();
        return new Token("" + op, switch (op) {
            case '=' -> TokenType.OP_ASSIGN;
            default -> throw new IllegalStateException("Unreachable");
        });
    }

    private boolean isIdentifierChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || isNumber(c) || isUnderscore(c);
    }

    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isOperator(char c) {
        return c == '=' || c == '+' || c == '-';
    }

    private boolean isUnderscore(char c) {
        return c == '_';
    }

    private boolean isSeparator(char c) {
        return c == 0 || isWhitespace(c) || isLineBreak(c);
    }

    private boolean isLineBreak(char c) {
        return c == '\n';
    }

    private boolean isWhitespace(char c) {
        return c == ' ';
    }
}
