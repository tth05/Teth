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
            emitUntil(c -> {
                if (c != 0)
                    return false;
                emit(this.stream.createCurrentIndexSpan(), "", TokenType.EOF);
                return true;
            });
        } catch (UnexpectedCharException e) {
            return new TokenizerResult(this.tokenStream, ProblemList.of(e.asProblem()));
        }

        return new TokenizerResult(this.tokenStream);
    }

    private void emitUntil(CharPredicate breakPredicate) {
        while (true) {
            char c = this.stream.peek();
            if (breakPredicate.test(c))
                break;

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
            } else if (isColon(c)) {
                emit(this.stream.consumeKnownSingle(), ":", TokenType.COLON);
            } else {
                throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Invalid character '%s'", c);
            }
        }
    }

    private void emit(Token token) {
        this.tokenStream.push(token);
    }

    private void emit(String value, TokenType type) {
        this.tokenStream.push(new Token(this.stream.popMarkedSpan(), value, type));
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

        return new Token(this.stream.popMarkedSpan(), ident.toString(), TokenType.IDENTIFIER);
    }

    private void emitString() {
        this.stream.markSpan();

        var string = new StringBuilder(5);
        this.stream.consume(); //Prefix

        var escaped = false;
        while (true) {
            var next = this.stream.peek();
            if (next == 0 || isLineBreak(next))
                throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Unclosed string literal");

            if (escaped) { // Escaped character
                //TODO: Actual escape sequences
                string.append(this.stream.consume());
                escaped = false;
            } else if (isQuote(next)) { // End string
                break;
            } else if (next == '\\') { // Escape next char
                escaped = true;
                this.stream.consume();
            } else if (next == '{') {
                // Commit everything up to this point as a string literal
                emit(string.toString(), TokenType.STRING_LITERAL);
                string.setLength(0);

                emit(this.stream.createCurrentIndexSpan(), "{", TokenType.STRING_LITERAL_CODE_START);
                this.stream.consume();
                emitUntil(new BracketMatchingBreakPredicate());
                emit(this.stream.createCurrentIndexSpan(), "}", TokenType.STRING_LITERAL_CODE_END);
                this.stream.consume();

                this.stream.markSpan();
            } else { // Normal character
                string.append(this.stream.consume());
            }
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

        var span = this.stream.popMarkedSpan();
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
                var next = this.stream.peek();
                switch (next) {
                    case '=' -> {
                        this.stream.consume();
                        emit(">=", TokenType.LESS_EQUAL);
                    }
                    case '|' -> {
                        this.stream.consume();
                        emit("<|", TokenType.LESS_PIPE);
                    }
                    default -> emit("<", TokenType.LESS);
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
            case '&' -> {
                if (this.stream.peek() == '&') {
                    this.stream.consume();
                    emit("&&", TokenType.AMPERSAND_AMPERSAND);
                } else {
                    throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Invalid character '&', did you mean '&&'");
                }
            }
            case '|' -> {
                if (this.stream.peek() == '|') {
                    this.stream.consume();
                    emit("||", TokenType.PIPE_PIPE);
                } else {
                    throw new UnexpectedCharException(this.stream.createCurrentIndexSpan(), "Invalid character '|', did you mean '||'");
                }
            }
            case '+' -> emit("+", TokenType.PLUS);
            case '-' -> emit("-", TokenType.MINUS);
            case '*' -> emit("*", TokenType.MULTIPLY);
            case '/' -> {
                if (this.stream.peek() == '/') {
                    this.stream.consume();
                    skipLineComment();
                } else if (this.stream.peek() == '*') {
                    this.stream.consume();
                    skipMultiLineComment();
                } else {
                    emit("/", TokenType.SLASH);
                }
            }
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
            case '[' -> TokenType.L_SQUARE_BRACKET;
            case ']' -> TokenType.R_SQUARE_BRACKET;
            default -> throw new IllegalStateException("Unreachable");
        });
    }

    private void skipLineComment() {
        while (this.stream.peek() != '\n')
            this.stream.consume();
    }

    private void skipMultiLineComment() {
        while (true) {
            var c = this.stream.consume();
            if (c == '*' && this.stream.peek() == '/') {
                this.stream.consume();
                return;
            }
        }
    }

    private static boolean isKeyword(String value) {
        return value.equals("if") || value.equals("else") || value.equals("fn") ||
               value.equals("return") || value.equals("let") || value.equals("loop") ||
               value.equals("new") || value.equals("struct") || value.equals("use");
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
        return c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '>' || c == '<' || c == '!' || c == '&' || c == '|';
    }

    private static boolean isQuote(char c) {
        return c == '"';
    }

    private static boolean isParen(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']';
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

    private static boolean isColon(char c) {
        return c == ':';
    }

    private static boolean isSeparator(char c) {
        return c == 0 || isWhitespace(c) || isLineBreak(c) || isOperator(c) || isParen(c) || isComma(c) || isColon(c);
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

    @FunctionalInterface
    private interface CharPredicate {

        boolean test(char c);
    }

    private static class BracketMatchingBreakPredicate implements CharPredicate {

        private int balance = 1;

        @Override
        public boolean test(char c) {
            if (c == '{')
                this.balance++;
            else if (c == '}')
                this.balance--;
            return this.balance == 0;
        }
    }
}
