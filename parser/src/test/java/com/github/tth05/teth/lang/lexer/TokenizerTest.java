package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.AbstractTokenizerTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest extends AbstractTokenizerTest {

    @Test
    public void testPositiveNumber() {
        createStreams("12345");
        assertIterableEquals(tokenList(new Token("12345", TokenType.NUMBER)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testNegativeNumber() {
        createStreams("-12345");
        assertIterableEquals(tokenList(
                new Token("-", TokenType.MINUS),
                new Token("12345", TokenType.NUMBER)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvalidNumber() {
        assertThrows(UnexpectedCharException.class, () -> createStreams("123anIdentifier"));
    }

    @Test
    public void testSimpleString() {
        createStreams("\"a cool string\"");
        assertIterableEquals(tokenList(new Token("a cool string", TokenType.STRING)), tokensIntoList());
        assertStreamsEmpty();

        createStreams("\"teth_is_great!\"");
        assertIterableEquals(tokenList(new Token("teth_is_great!", TokenType.STRING)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testUnclosedString() {
        assertThrows(UnexpectedCharException.class, () -> createStreams("\"123anIdentifier"));
        assertThrows(UnexpectedCharException.class, () -> createStreams("\"123anIdentifier\n"));
    }

    @Test
    public void testIdentifier() {
        createStreams("anIdentifier");
        assertIterableEquals(tokenList(new Token("anIdentifier", TokenType.IDENTIFIER)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvalidChars() {
        var matcher = Pattern.compile("^[a-zA-Z0-9+\\-*/^_,=\\t\\n<>! (){}]$").matcher("");
        for (int i = 1; i < 1000; i++) {
            var str = "" + (char) i;
            if (matcher.reset(str).matches())
                continue;

            assertThrows(UnexpectedCharException.class, () -> createStreams(str), "No exception thrown for '" + str + "' " + i);
        }
    }

    @Test
    public void testAssign() {
        createStreams("type anIdentifier = 56");
        assertIterableEquals(tokenList(
                new Token("type", TokenType.IDENTIFIER),
                new Token("anIdentifier", TokenType.IDENTIFIER),
                new Token("=", TokenType.EQUAL),
                new Token("56", TokenType.NUMBER)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testAssignMultiline() {
        createStreams("type anIdentifier\n =\n 56");
        assertIterableEquals(tokenList(
                new Token("type", TokenType.IDENTIFIER),
                new Token("anIdentifier", TokenType.IDENTIFIER),
                new Token("\n", TokenType.LINE_BREAK),
                new Token("=", TokenType.EQUAL),
                new Token("\n", TokenType.LINE_BREAK),
                new Token("56", TokenType.NUMBER)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testMathExpression() {
        createStreams("5*(1^2-45)/10+1");
        assertIterableEquals(tokenList(
                new Token("5", TokenType.NUMBER),
                new Token("*", TokenType.MULTIPLY),
                new Token("(", TokenType.L_PAREN),
                new Token("1", TokenType.NUMBER),
                new Token("^", TokenType.POW),
                new Token("2", TokenType.NUMBER),
                new Token("-", TokenType.MINUS),
                new Token("45", TokenType.NUMBER),
                new Token(")", TokenType.R_PAREN),
                new Token("/", TokenType.DIVIDE),
                new Token("10", TokenType.NUMBER),
                new Token("+", TokenType.PLUS),
                new Token("1", TokenType.NUMBER)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testIfStatement() {
        createStreams("if (1 == 2) { 3 }");
        assertIterableEquals(tokenList(
                new Token("if", TokenType.KEYWORD),
                new Token("(", TokenType.L_PAREN),
                new Token("1", TokenType.NUMBER),
                new Token("==", TokenType.EQUAL_EQUAL),
                new Token("2", TokenType.NUMBER),
                new Token(")", TokenType.R_PAREN),
                new Token("{", TokenType.L_CURLY_PAREN),
                new Token("3", TokenType.NUMBER),
                new Token("}", TokenType.R_CURLY_PAREN)
        ), tokensIntoList());
        assertStreamsEmpty();

        createStreams("if (1 == 2) { 3 } else 5 == 5");
        assertIterableEquals(tokenList(
                new Token("if", TokenType.KEYWORD),
                new Token("(", TokenType.L_PAREN),
                new Token("1", TokenType.NUMBER),
                new Token("==", TokenType.EQUAL_EQUAL),
                new Token("2", TokenType.NUMBER),
                new Token(")", TokenType.R_PAREN),
                new Token("{", TokenType.L_CURLY_PAREN),
                new Token("3", TokenType.NUMBER),
                new Token("}", TokenType.R_CURLY_PAREN),
                new Token("else", TokenType.KEYWORD),
                new Token("5", TokenType.NUMBER),
                new Token("==", TokenType.EQUAL_EQUAL),
                new Token("5", TokenType.NUMBER)
        ), tokensIntoList());
        assertStreamsEmpty();

        createStreams("if (1 == 2) 3 else if (a) 5");
        assertIterableEquals(tokenList(
                new Token("if", TokenType.KEYWORD),
                new Token("(", TokenType.L_PAREN),
                new Token("1", TokenType.NUMBER),
                new Token("==", TokenType.EQUAL_EQUAL),
                new Token("2", TokenType.NUMBER),
                new Token(")", TokenType.R_PAREN),
                new Token("3", TokenType.NUMBER),
                new Token("else", TokenType.KEYWORD),
                new Token("if", TokenType.KEYWORD),
                new Token("(", TokenType.L_PAREN),
                new Token("a", TokenType.IDENTIFIER),
                new Token(")", TokenType.R_PAREN),
                new Token("5", TokenType.NUMBER)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    private List<Token> tokenList(Token... tokens) {
        List<Token> list = new ArrayList<>(Arrays.asList(tokens));
        list.add(new Token("", TokenType.EOF));
        return list;
    }
}
