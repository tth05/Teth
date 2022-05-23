package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.Problem;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.List;

public class Tokenizer {

    private final CharStream stream;
    private final TokenStream tokenStream = new TokenStream();

    private Tokenizer(CharStream stream) {
        this.stream = stream;
    }

    public TokenizerResult tokenize() {
        try {
            while (true) {
                char c = this.stream.peek();
                if (c == 0) {
                    emit("", TokenType.EOF);
                    break;
                }

                if (isNumber(c)) {
                    emitNumber();
                } else if (isQuote(c)) {
                    emitString();
                } else if (isIdentifierChar(c)) {
                    var ident = parseIdentifier();
                    if (isKeyword(ident.value()))
                        emit(new Token(ident.span(), ident.value(), TokenType.KEYWORD));
                    else if (isBooleanLiteral(ident.value()))
                        emit(new Token(ident.span(), ident.value(), TokenType.BOOLEAN_LITERAL));
                    else
                        emit(ident);
                } else if (isOperator(c)) {
                    emitOperator();
                } else if (isParen(c)) {
                    emitParen();
                } else if (isLineBreak(c)) {
                    emit("\n", TokenType.LINE_BREAK);
                    this.stream.consume();
                } else if (isWhitespace(c)) {
                    this.stream.consume();
                } else if (isComma(c)) {
                    emit(",", TokenType.COMMA);
                    this.stream.consume();
                } else if (isDot(c)) {
                    emit(".", TokenType.DOT);
                    this.stream.consume();
                } else {
                    throw new UnexpectedCharException(this.stream.getSpan(), c);
                }
            }
        } catch (UnexpectedCharException e) {
            return new TokenizerResult(this.tokenStream, List.of(new Problem(e.getSpan(), e.getMessage())));
        }

        return new TokenizerResult(this.tokenStream);
    }

    private void emit(Token token) {
        this.tokenStream.push(token);
    }

    private void emit(String value, TokenType type) {
        this.tokenStream.push(new Token(this.stream.getSpan(), value, type));
    }

    private void emit(ISpan span, String value, TokenType type) {
        this.tokenStream.push(new Token(span, value, type));
    }

    private Token parseIdentifier() {
        var span = this.stream.getSpan();
        StringBuilder ident = new StringBuilder(8);
        do {
            char c = this.stream.consume();
            if (!isIdentifierChar(c))
                throw new UnexpectedCharException(this.stream.getSpan(), c, TokenType.IDENTIFIER);

            ident.append(c);
        } while (!isSeparator(this.stream.peek()) && !isDot(this.stream.peek()));

        return new Token(span, ident.toString(), TokenType.IDENTIFIER);
    }

    private void emitString() {
        var span = this.stream.getSpan();
        StringBuilder string = new StringBuilder(5);
        this.stream.consume(); //Prefix
        while (true) {
            var next = this.stream.peek();
            if (next == 0 || isLineBreak(next))
                throw new UnexpectedCharException(this.stream.getSpan(), next, TokenType.STRING_LITERAL);
            if (isQuote(next))
                break;

            //TODO: Escapes etc.
            string.append(this.stream.consume());
        }
        this.stream.consume(); //Suffix

        emit(span, string.toString(), TokenType.STRING_LITERAL);
    }

    private void emitNumber() {
        var span = this.stream.getSpan();
        boolean isDouble = false;
        StringBuilder number = new StringBuilder(2);
        do {
            char c = this.stream.consume();

            if (c == '.') {
                if (isDouble)
                    throw new UnexpectedCharException(this.stream.getSpan(), c, TokenType.LONG_LITERAL);
                isDouble = true;
                number.append('.');
                continue;
            }

            if (!isNumber(c))
                throw new UnexpectedCharException(this.stream.getSpan(), c, TokenType.LONG_LITERAL);

            number.append(c);
        } while (!isSeparator(this.stream.peek()));


        if (isDouble)
            emit(span, number.toString(), TokenType.DOUBLE_LITERAL);
        else
            emit(span, number.toString(), TokenType.LONG_LITERAL);
    }

    private void emitOperator() {
        var op = this.stream.peek();
        switch (op) {
            case '=' -> {
                if (this.stream.peek(1) == '=') {
                    emit("==", TokenType.EQUAL_EQUAL);
                    this.stream.consume();
                } else {
                    emit("=", TokenType.EQUAL);
                }
            }
            case '!' -> {
                if (this.stream.peek(1) == '=') {
                    emit("!=", TokenType.NOT_EQUAL);
                    this.stream.consume();
                } else {
                    emit("!", TokenType.NOT);
                }
            }
            case '<' -> {
                if (this.stream.peek(1) == '=') {
                    emit("<=", TokenType.LESS_EQUAL);
                    this.stream.consume();
                } else {
                    emit("<", TokenType.LESS);
                }
            }
            case '>' -> {
                if (this.stream.peek(1) == '=') {
                    emit(">=", TokenType.GREATER_EQUAL);
                    this.stream.consume();
                } else {
                    emit(">", TokenType.GREATER);
                }
            }
            case '+' -> emit("+", TokenType.PLUS);
            case '-' -> emit("-", TokenType.MINUS);
            case '*' -> emit("*", TokenType.MULTIPLY);
            case '/' -> emit("/", TokenType.DIVIDE);
            case '^' -> emit("^", TokenType.POW);
            default -> throw new IllegalStateException("Unreachable");
        }

        this.stream.consume();
    }

    private void emitParen() {
        var span = this.stream.getSpan();
        var c = this.stream.consume();
        emit(span, "" + c, switch (c) {
            case '(' -> TokenType.L_PAREN;
            case ')' -> TokenType.R_PAREN;
            case '{' -> TokenType.L_CURLY_PAREN;
            case '}' -> TokenType.R_CURLY_PAREN;
            default -> throw new IllegalStateException("Unreachable");
        });
    }

    private static boolean isKeyword(String value) {
        return value.equals("if") || value.equals("else") || value.equals("fn") || value.equals("return");
    }

    private static boolean isBooleanLiteral(String value) {
        return value.equals("true") || value.equals("false");
    }

    private static boolean isIdentifierChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || isNumber(c) || isUnderscore(c);
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isOperator(char c) {
        return c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '>' || c == '<' || c == '!';
    }

    private static boolean isQuote(char c) {
        return c == '"';
    }

    private static boolean isParen(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}';
    }

    private static boolean isUnderscore(char c) {
        return c == '_';
    }

    private static boolean isComma(char c) {
        return c == ',';
    }

    private static boolean isDot(char c) {
        return c == '.';
    }

    private static boolean isSeparator(char c) {
        return c == 0 || isWhitespace(c) || isLineBreak(c) || isOperator(c) || isParen(c) || isComma(c);
    }

    private static boolean isLineBreak(char c) {
        return c == '\n';
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    public static TokenizerResult streamOf(CharStream charStream) {
        return new Tokenizer(charStream).tokenize();
    }
}
