package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.stream.CharStream;

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
                    emit(this.stream.createCurrentIndexSpan(), "", TokenType.EOF);
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
                    emit(this.stream.consumeKnownSingle(), "\n", TokenType.LINE_BREAK);
                } else if (isWhitespace(c)) {
                    this.stream.consume();
                } else if (isComma(c)) {
                    emit(this.stream.consumeKnownSingle(), ",", TokenType.COMMA);
                } else if (isDot(c)) {
                    emit(this.stream.consumeKnownSingle(), ".", TokenType.DOT);
                } else {
                    throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Invalid character '%s'", c);
                }
            }
        } catch (UnexpectedCharException e) {
            return new TokenizerResult(this.tokenStream, ProblemList.of(e.asProblem()));
        }

        return new TokenizerResult(this.tokenStream);
    }

    private void emit(Token token) {
        this.tokenStream.push(token);
    }

    private void emit(String value, TokenType type) {
        this.tokenStream.push(new Token(this.stream.createMarkedSpan(), value, type));
    }

    private void emit(ISpan span, String value, TokenType type) {
        this.tokenStream.push(new Token(span, value, type));
    }

    private Token parseIdentifier() {
        this.stream.markSpan();
        StringBuilder ident = new StringBuilder(8);
        do {
            char c = this.stream.peek();
            if (!isIdentifierChar(c))
                throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Invalid character '%s' in identifier", c);

            ident.append(this.stream.consume());
        } while (!isSeparator(this.stream.peek()) && !isDot(this.stream.peek()));

        return new Token(this.stream.createMarkedSpan(), ident.toString(), TokenType.IDENTIFIER);
    }

    private void emitString() {
        this.stream.markSpan();

        StringBuilder string = new StringBuilder(5);
        this.stream.consume(); //Prefix
        while (true) {
            var next = this.stream.peek();
            if (next == 0 || isLineBreak(next))
                throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Unclosed string literal");
            if (isQuote(next))
                break;

            //TODO: Escapes etc.
            string.append(this.stream.consume());
        }
        this.stream.consume(); //Suffix

        emit(string.toString(), TokenType.STRING_LITERAL);
    }

    private void emitNumber() {
        this.stream.markSpan();

        boolean isDouble = false;
        StringBuilder number = new StringBuilder(2);
        do {
            char c = this.stream.peek();

            if (c == '.') {
                this.stream.consume();
                if (isDouble)
                    throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Second decimal point in number literal");
                isDouble = true;
                number.append('.');
                continue;
            }

            if (!isNumber(c))
                throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Invalid character '%s' in number literal", c);

            number.append(this.stream.consume());
        } while (!isSeparator(this.stream.peek()));

        var span = this.stream.createMarkedSpan();
        try {
            if (isDouble)
                Double.parseDouble(number.toString());
            else
                Long.parseLong(number.toString());
        } catch (NumberFormatException e) {
            throw new UnexpectedCharException(span, "Number is too big");
        }

        emit(span, number.toString(), isDouble ? TokenType.DOUBLE_LITERAL : TokenType.LONG_LITERAL);
    }

    private void emitOperator() {
        this.stream.markSpan();

        var op = this.stream.consume();
        switch (op) {
            case '=' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    emit("==", TokenType.EQUAL_EQUAL);
                } else {
                    emit("=", TokenType.EQUAL);
                }
            }
            case '!' -> {
                if (this.stream.peek() == '=') {
                    emit("!=", TokenType.NOT_EQUAL);
                    this.stream.consume();
                } else {
                    emit("!", TokenType.NOT);
                }
            }
            case '<' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    emit("<=", TokenType.LESS_EQUAL);
                } else {
                    emit("<", TokenType.LESS);
                }
            }
            case '>' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    emit(">=", TokenType.GREATER_EQUAL);
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
    }

    private void emitParen() {
        var span = this.stream.createCurrentIndexSpan();
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
