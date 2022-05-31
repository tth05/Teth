package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.AbstractTokenizerTest;
import com.github.tth05.teth.lang.span.Span;
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
        assertIterableEquals(tokenList(new Token(makeSpan(0, 5), "12345", TokenType.LONG_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
        createStreams("12345.12345");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 11), "12345.12345", TokenType.DOUBLE_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testNegativeNumber() {
        createStreams("-12345");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), "-", TokenType.MINUS),
                new Token(makeSpan(1, 6), "12345", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
        createStreams("-12345.54");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), "-", TokenType.MINUS),
                new Token(makeSpan(1, 9), "12345.54", TokenType.DOUBLE_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvalidNumber() {
        assertThrows(Exception.class, () -> createStreams("123anIdentifier"));
    }

    @Test
    public void testSimpleString() {
        createStreams("\"a cool string\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 15), "a cool string", TokenType.STRING_LITERAL)), tokensIntoList());
        assertStreamsEmpty();

        createStreams("\"teth_is_great!\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 16), "teth_is_great!", TokenType.STRING_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testUnclosedString() {
        assertThrows(Exception.class, () -> createStreams("\"123anIdentifier"));
        assertThrows(Exception.class, () -> createStreams("\"123anIdentifier\n"));
    }

    @Test
    public void testBoolean() {
        createStreams("true");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 4), "true", TokenType.BOOLEAN_LITERAL)), tokensIntoList());
        assertStreamsEmpty();

        createStreams("false");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 5), "false", TokenType.BOOLEAN_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testIdentifier() {
        createStreams("anIdentifier");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 12), "anIdentifier", TokenType.IDENTIFIER)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testListLiteral() {
        createStreams("[5 + 5, true, \"str\"]");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), "[", TokenType.L_SQUARE_PAREN),
                new Token(makeSpan(1, 2), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(3, 4), "+", TokenType.PLUS),
                new Token(makeSpan(5, 6), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 7), ",", TokenType.COMMA),
                new Token(makeSpan(8, 12), "true", TokenType.BOOLEAN_LITERAL),
                new Token(makeSpan(12, 13), ",", TokenType.COMMA),
                new Token(makeSpan(14, 19), "str", TokenType.STRING_LITERAL),
                new Token(makeSpan(19, 20), "]", TokenType.R_SQUARE_PAREN)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvalidChars() {
        var matcher = Pattern.compile("^[a-zA-Z0-9+\\-*/^_.,=\\t\\n<>! (){}\\[\\]]$").matcher("");
        for (int i = 1; i < 1000; i++) {
            var str = "" + (char) i;
            if (matcher.reset(str).matches())
                continue;

            assertThrows(Exception.class, () -> createStreams(str), "No exception thrown for '" + str + "' " + i);
        }
    }

    @Test
    public void testAssign() {
        createStreams("type anIdentifier = 56");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 4), "type", TokenType.IDENTIFIER),
                new Token(makeSpan(5, 17), "anIdentifier", TokenType.IDENTIFIER),
                new Token(makeSpan(18, 19), "=", TokenType.EQUAL),
                new Token(makeSpan(20, 22), "56", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testAssignMultiline() {
        createStreams("type anIdentifier\n =\n 56");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 4), "type", TokenType.IDENTIFIER),
                new Token(makeSpan(5, 17), "anIdentifier", TokenType.IDENTIFIER),
                new Token(makeSpan(17, 18), "\n", TokenType.LINE_BREAK),
                new Token(makeSpan(19, 20), "=", TokenType.EQUAL),
                new Token(makeSpan(20, 21), "\n", TokenType.LINE_BREAK),
                new Token(makeSpan(22, 24), "56", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testMathExpression() {
        createStreams("5*(1^2-45)/10+1.01");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(1, 2), "*", TokenType.MULTIPLY),
                new Token(makeSpan(2, 3), "(", TokenType.L_PAREN),
                new Token(makeSpan(3, 4), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(4, 5), "^", TokenType.POW),
                new Token(makeSpan(5, 6), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 7), "-", TokenType.MINUS),
                new Token(makeSpan(7, 9), "45", TokenType.LONG_LITERAL),
                new Token(makeSpan(9, 10), ")", TokenType.R_PAREN),
                new Token(makeSpan(10, 11), "/", TokenType.DIVIDE),
                new Token(makeSpan(11, 13), "10", TokenType.LONG_LITERAL),
                new Token(makeSpan(13, 14), "+", TokenType.PLUS),
                new Token(makeSpan(14, 18), "1.01", TokenType.DOUBLE_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testIfStatement() {
        createStreams("if (1 == 2) { 3 }");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 4), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 5), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 8), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 10), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), ")", TokenType.R_PAREN),
                new Token(makeSpan(12, 13), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(14, 15), "3", TokenType.LONG_LITERAL),
                new Token(makeSpan(16, 17), "}", TokenType.R_CURLY_PAREN)
        ), tokensIntoList());
        assertStreamsEmpty();

        createStreams("if (1 == 2) { 35.45 } else 5 == 5");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 4), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 5), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 8), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 10), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), ")", TokenType.R_PAREN),
                new Token(makeSpan(12, 13), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(14, 19), "35.45", TokenType.DOUBLE_LITERAL),
                new Token(makeSpan(20, 21), "}", TokenType.R_CURLY_PAREN),
                new Token(makeSpan(22, 26), "else", TokenType.KEYWORD),
                new Token(makeSpan(27, 28), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(29, 31), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(32, 33), "5", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();

        createStreams("if (1 == 2) 3 else if (a) 5");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 4), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 5), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 8), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 10), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), ")", TokenType.R_PAREN),
                new Token(makeSpan(12, 13), "3", TokenType.LONG_LITERAL),
                new Token(makeSpan(14, 18), "else", TokenType.KEYWORD),
                new Token(makeSpan(19, 21), "if", TokenType.KEYWORD),
                new Token(makeSpan(22, 23), "(", TokenType.L_PAREN),
                new Token(makeSpan(23, 24), "a", TokenType.IDENTIFIER),
                new Token(makeSpan(24, 25), ")", TokenType.R_PAREN),
                new Token(makeSpan(26, 27), "5", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    private static List<Token> tokenList(Token... tokens) {
        List<Token> list = new ArrayList<>(Arrays.asList(tokens));

        var lastToken = list.get(list.size() - 1);
        var lastSpan = lastToken.span();

        list.add(new Token(new Span(lastSpan.source(), lastSpan.source().length, lastSpan.source().length + 1), "", TokenType.EOF));
        return list;
    }

    private static int countLineBreaks(char[] source, int start) {
        int count = 0;
        for (int i = start; i < source.length; i++) {
            if (source[i] == '\n')
                count++;
        }
        return count;
    }
}
