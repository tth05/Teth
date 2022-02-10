package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.stream.CharStream;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    @Test
    public void testNumber() {
        var positive = new Tokenizer(CharStream.fromString("12345")).tokenize();
        var negative = new Tokenizer(CharStream.fromString("-12345")).tokenize();

        assertIterableEquals(tokenList(new Token("12345", TokenType.NUMBER)), positive);
        assertIterableEquals(tokenList(
                new Token("-", TokenType.OP_MINUS),
                new Token("12345", TokenType.NUMBER)
        ), negative);
    }

    @Test
    public void testInvalidNumber() {
        assertThrows(UnexpectedCharException.class, () -> new Tokenizer(CharStream.fromString("123anIdentifier")).tokenize());
    }

    @Test
    public void testIdentifier() {
        var tokens = new Tokenizer(CharStream.fromString("anIdentifier")).tokenize();
        assertIterableEquals(tokenList(new Token("anIdentifier", TokenType.IDENTIFIER)), tokens);
    }

    @Test
    public void testInvalidChars() {
        var matcher = Pattern.compile("^[a-zA-Z0-9+\\-*/^_=\\n ()]$").matcher("");
        for (int i = 1; i < 1000; i++) {
            var str = "" + (char) i;
            if (matcher.reset(str).matches())
                continue;

            assertThrows(UnexpectedCharException.class, () -> new Tokenizer(CharStream.fromString(str)).tokenize(), "No exception thrown for '" + str + "' " + i);
        }
    }

    @Test
    public void testAssign() {
        var tokens = new Tokenizer(CharStream.fromString("type anIdentifier = 56")).tokenize();
        assertIterableEquals(tokenList(
                new Token("type", TokenType.IDENTIFIER),
                new Token("anIdentifier", TokenType.IDENTIFIER),
                new Token("=", TokenType.OP_ASSIGN),
                new Token("56", TokenType.NUMBER)
        ), tokens);
    }

    @Test
    public void testAssignMultiline() {
        var tokens = new Tokenizer(CharStream.fromString("type anIdentifier\n =\n 56")).tokenize();
        assertIterableEquals(tokenList(
                new Token("type", TokenType.IDENTIFIER),
                new Token("anIdentifier", TokenType.IDENTIFIER),
                new Token("\n", TokenType.LINE_BREAK),
                new Token("=", TokenType.OP_ASSIGN),
                new Token("\n", TokenType.LINE_BREAK),
                new Token("56", TokenType.NUMBER)
        ), tokens);
    }

    @Test
    public void testMathExpression() {
        var tokens = new Tokenizer(CharStream.fromString("5*(1^2-45)/10+1")).tokenize();
        assertIterableEquals(tokenList(
                new Token("5", TokenType.NUMBER),
                new Token("*", TokenType.OP_STAR),
                new Token("(", TokenType.L_PAREN),
                new Token("1", TokenType.NUMBER),
                new Token("^", TokenType.OP_ROOF),
                new Token("2", TokenType.NUMBER),
                new Token("-", TokenType.OP_MINUS),
                new Token("45", TokenType.NUMBER),
                new Token(")", TokenType.R_PAREN),
                new Token("/", TokenType.OP_SLASH),
                new Token("10", TokenType.NUMBER),
                new Token("+", TokenType.OP_PLUS),
                new Token("1", TokenType.NUMBER)
        ), tokens);
    }

    private List<Token> tokenList(Token... tokens) {
        List<Token> list = new ArrayList<>(Arrays.asList(tokens));
        list.add(new Token("", TokenType.EOF));
        return list;
    }
}
