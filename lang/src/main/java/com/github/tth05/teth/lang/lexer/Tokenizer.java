package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.Problem;
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
                    emit(new Token("", TokenType.EOF));
                    break;
                }

                if (isNumber(c)) {
                    emit(parseNumber());
                } else if (isQuote(c)) {
                    emit(parseString());
                } else if (isIdentifierChar(c)) {
                    var ident = parseIdentifier();
                    if (isKeyword(ident.value()))
                        ident = new Token(ident.value(), TokenType.KEYWORD);
                    else if (isBooleanLiteral(ident.value()))
                        ident = new Token(ident.value(), TokenType.BOOLEAN_LITERAL);
                    emit(ident);
                } else if (isOperator(c)) {
                    emit(parseOperator());
                } else if (isParen(c)) {
                    emit(parseParen());
                } else if (isLineBreak(c)) {
                    this.stream.consume();
                    emit(new Token("\n", TokenType.LINE_BREAK));
                } else if (isWhitespace(c)) {
                    this.stream.consume();
                } else if (isComma(c)) {
                    this.stream.consume();
                    emit(new Token(",", TokenType.COMMA));
                } else if (isDot(c)) {
                    this.stream.consume();
                    emit(new Token(".", TokenType.DOT));
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

    private Token parseIdentifier() {
        StringBuilder ident = new StringBuilder(8);
        do {
            char c = this.stream.consume();
            if (!isIdentifierChar(c))
                throw new UnexpectedCharException(this.stream.getSpan(), c, TokenType.IDENTIFIER);

            ident.append(c);
        } while (!isSeparator(this.stream.peek()) && !isDot(this.stream.peek()));

        return new Token(ident.toString(), TokenType.IDENTIFIER);
    }

    private Token parseString() {
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

        return new Token(string.toString(), TokenType.STRING_LITERAL);
    }

    private Token parseNumber() {
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


        return isDouble ? new Token(number.toString(), TokenType.DOUBLE_LITERAL) : new Token(number.toString(), TokenType.LONG_LITERAL);
    }

    private Token parseOperator() {
        var op = this.stream.consume();
        return switch (op) {
            case '=' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    yield new Token("==", TokenType.EQUAL_EQUAL);
                }

                yield new Token("=", TokenType.EQUAL);
            }
            case '!' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    yield new Token("!=", TokenType.NOT_EQUAL);
                }

                yield new Token("!", TokenType.NOT);
            }
            case '<' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    yield new Token("<=", TokenType.LESS_EQUAL);
                }

                yield new Token("<", TokenType.LESS);
            }
            case '>' -> {
                if (this.stream.peek() == '=') {
                    this.stream.consume();
                    yield new Token(">=", TokenType.GREATER_EQUAL);
                }

                yield new Token(">", TokenType.GREATER);
            }
            case '+' -> new Token("+", TokenType.PLUS);
            case '-' -> new Token("-", TokenType.MINUS);
            case '*' -> new Token("*", TokenType.MULTIPLY);
            case '/' -> new Token("/", TokenType.DIVIDE);
            case '^' -> new Token("^", TokenType.POW);
            default -> throw new IllegalStateException("Unreachable");
        };
    }

    private Token parseParen() {
        var c = this.stream.consume();
        return new Token("" + c, switch (c) {
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
