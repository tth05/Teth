package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.Problem;
import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

public class Tokenizer {

    private final StringBuilder identifierBuffer = new StringBuilder(8);
    private final ProblemList problems = new ProblemList();

    private final CharStream stream;
    private final TokenStream tokenStream;

    private Tokenizer(CharStream stream) {
        this.stream = stream;
        this.tokenStream = new TokenStream(stream.getSource());
    }

    public TokenizerResult tokenize() {
        emitUntil(c -> {
            if (c != 0)
                return false;
            emit(this.stream.createCurrentIndexSpan(), "", TokenType.EOF);
            return true;
        });

        return new TokenizerResult(this.tokenStream, this.problems);
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
                    emitKeyword(ident);
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
                emit(this.stream.consumeKnownSingle(), " ", TokenType.WHITESPACE);
            } else if (isComma(c)) {
                emit(this.stream.consumeKnownSingle(), ",", TokenType.COMMA);
            } else if (isDot(c)) {
                emit(this.stream.consumeKnownSingle(), ".", TokenType.DOT);
            } else if (isColon(c)) {
                emit(this.stream.consumeKnownSingle(), ":", TokenType.COLON);
            } else {
                var span = this.stream.createCurrentIndexSpan();
                this.stream.consume();

                emit(span, c + "", TokenType.INVALID);
                report(span, "Invalid character '" + c + "'");
            }
        }
    }

    private void emitKeyword(Token ident) {
        emit(new Token(ident.span(), ident.value(), switch (ident.value()) {
            case "if" -> TokenType.KEYWORD_IF;
            case "else" -> TokenType.KEYWORD_ELSE;
            case "fn" -> TokenType.KEYWORD_FN;
            case "return" -> TokenType.KEYWORD_RETURN;
            case "let" -> TokenType.KEYWORD_LET;
            case "loop" -> TokenType.KEYWORD_LOOP;
            case "break" -> TokenType.KEYWORD_BREAK;
            case "continue" -> TokenType.KEYWORD_CONTINUE;
            case "new" -> TokenType.KEYWORD_NEW;
            case "struct" -> TokenType.KEYWORD_STRUCT;
            case "use" -> TokenType.KEYWORD_USE;
            case "null" -> TokenType.KEYWORD_NULL;
            default -> throw new IllegalStateException("Unexpected value: " + ident.value());
        }));
    }

    private void emit(Token token) {
        this.tokenStream.push(token);
    }

    private void emit(String value, TokenType type) {
        this.tokenStream.push(new Token(this.stream.popMarkedSpan(), value, type));
    }

    private void emit(Span span, String value, TokenType type) {
        this.tokenStream.push(new Token(span, value, type));
    }

    private Token parseIdentifier() {
        this.identifierBuffer.setLength(0);

        this.stream.markSpan();
        do {
            char c = this.stream.peek();
            if (!isIdentifierChar(c))
                report(this.stream.createCurrentIndexSpan(), "Invalid character '" + c + "' in identifier");
            else
                this.identifierBuffer.append(c);

            this.stream.consume();
        } while (!isSeparator(this.stream.peek()) && !isDot(this.stream.peek()));

        return new Token(this.stream.popMarkedSpan(), this.identifierBuffer.toString(), TokenType.IDENTIFIER);
    }

    private void emitString() {
        this.stream.markSpan();

        var string = new StringBuilder(5);
        this.stream.consume(); //Prefix

        var escaped = false;
        while (true) {
            var next = this.stream.peek();
            if (next == 0 || isLineBreak(next)) {
                report(this.stream.createCurrentIndexSpan(), "Unclosed string literal");
                break;
            }

            if (escaped) { // Escaped character
                //TODO: Actual escape sequences
                string.append(this.stream.consume());
                escaped = false;
            } else if (isQuote(next)) { // End string
                this.stream.consume(); //Suffix
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
                if (this.stream.peek() != '}') {
                    report(this.stream.createCurrentIndexSpan(), "Unclosed string literal code block");
                    return;
                }

                emit(this.stream.createCurrentIndexSpan(), "}", TokenType.STRING_LITERAL_CODE_END);
                this.stream.consume();

                this.stream.markSpan();
            } else { // Normal character
                string.append(this.stream.consume());
            }
        }

        emit(string.toString(), TokenType.STRING_LITERAL);
    }

    private void emitNumber() {
        this.identifierBuffer.setLength(0);
        this.stream.markSpan();

        boolean isDouble = false;
        do {
            char c = this.stream.peek();

            if (c == '.') {
                // Second dot not allowed in number, stop here
                if (isDouble)
                    break;

                this.stream.consume();
                isDouble = true;
                this.identifierBuffer.append('.');
                continue;
            }

            if (!isNumber(c)) {
                report(this.stream.createCurrentIndexSpan(), "Invalid character '" + c + "' in number literal");
                break;
            }

            this.identifierBuffer.append(this.stream.consume());
        } while (!isSeparator(this.stream.peek()));

        var numberString = this.identifierBuffer.toString();
        var span = this.stream.popMarkedSpan();
        try {
            if (isDouble)
                Double.parseDouble(numberString);
            else
                Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            report(span, "Number is too big");
            numberString = "1";
        }

        emit(span, numberString, isDouble ? TokenType.DOUBLE_LITERAL : TokenType.LONG_LITERAL);
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
                if (this.stream.peek() != '&')
                    report(this.stream.createCurrentIndexSpan(), "Invalid character '&', did you mean '&&'?");

                this.stream.consume();
                emit("&&", TokenType.AMPERSAND_AMPERSAND);
            }
            case '|' -> {
                if (this.stream.peek() != '|')
                    report(this.stream.createCurrentIndexSpan(), "Invalid character '|', did you mean '||'?");

                this.stream.consume();
                emit("||", TokenType.PIPE_PIPE);
            }
            case '+' -> emit("+", TokenType.PLUS);
            case '-' -> emit("-", TokenType.MINUS);
            case '*' -> emit("*", TokenType.MULTIPLY);
            case '/' -> {
                if (this.stream.peek() == '/') {
                    this.stream.consume();
                    emitLineComment();
                } else if (this.stream.peek() == '*') {
                    this.stream.consume();
                    emitMultiLineComment();
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

    private void emitLineComment() {
        this.identifierBuffer.setLength(0);
        this.identifierBuffer.append("//");

        char current;
        while ((current = this.stream.peek()) != '\n' && current != 0)
            this.identifierBuffer.append(this.stream.consume());

        emit(this.stream.popMarkedSpan(), this.identifierBuffer.toString(), TokenType.COMMENT);
    }

    private void emitMultiLineComment() {
        this.identifierBuffer.setLength(0);
        this.identifierBuffer.append("/*");

        while (this.stream.peek() != 0) {
            var c = this.stream.consume();
            if (c == '*' && this.stream.peek() == '/') {
                this.stream.consume();
                break;
            }

            this.identifierBuffer.append(c);
        }

        this.identifierBuffer.append("*/");
        emit(this.stream.popMarkedSpan(), this.identifierBuffer.toString(), TokenType.COMMENT);
    }

    private void report(Span span, String message) {
        this.problems.add(new Problem(span, message));
    }

    public static boolean isKeyword(String value) {
        return value.equals("if") || value.equals("else") || value.equals("fn") || value.equals("return") ||
               value.equals("let") || value.equals("loop") || value.equals("break") || value.equals("continue") ||
               value.equals("new") || value.equals("struct") || value.equals("use") || value.equals("null");
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

    public static TokenizerResult tokenize(ISource source) {
        return new Tokenizer(CharStream.fromSource(source)).tokenize();
    }

    public static TokenizerResult tokenize(CharStream charStream) {
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
            return c == 0 || this.balance == 0;
        }
    }
}
