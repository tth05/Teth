package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.AbstractTokenizerTest;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.CharArrayUtils;
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
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "12345", TokenType.LONG_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
        createStreams("12345.12345");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "12345.12345", TokenType.DOUBLE_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testNegativeNumber() {
        createStreams("-12345");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "-", TokenType.MINUS),
                new Token(makeSpan(1, 1, 0, 1), "12345", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
        createStreams("-12345.54");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "-", TokenType.MINUS),
                new Token(makeSpan(1, 1, 0, 1), "12345.54", TokenType.DOUBLE_LITERAL)
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
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "a cool string", TokenType.STRING_LITERAL)), tokensIntoList());
        assertStreamsEmpty();

        createStreams("\"teth_is_great!\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "teth_is_great!", TokenType.STRING_LITERAL)), tokensIntoList());
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
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "true", TokenType.BOOLEAN_LITERAL)), tokensIntoList());
        assertStreamsEmpty();

        createStreams("false");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "false", TokenType.BOOLEAN_LITERAL)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testIdentifier() {
        createStreams("anIdentifier");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 1, 0, 0), "anIdentifier", TokenType.IDENTIFIER)), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvalidChars() {
        var matcher = Pattern.compile("^[a-zA-Z0-9+\\-*/^_.,=\\t\\n<>! (){}]$").matcher("");
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
                new Token(makeSpan(0, 1, 0, 0), "type", TokenType.IDENTIFIER),
                new Token(makeSpan(5, 1, 0, 5), "anIdentifier", TokenType.IDENTIFIER),
                new Token(makeSpan(18, 1, 0, 18), "=", TokenType.EQUAL),
                new Token(makeSpan(20, 1, 0, 20), "56", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testAssignMultiline() {
        createStreams("type anIdentifier\n =\n 56");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "type", TokenType.IDENTIFIER),
                new Token(makeSpan(5, 1, 0, 5), "anIdentifier", TokenType.IDENTIFIER),
                new Token(makeSpan(17, 1, 0, 17), "\n", TokenType.LINE_BREAK),
                new Token(makeSpan(19, 1, 1, 1), "=", TokenType.EQUAL),
                new Token(makeSpan(20, 1, 1, 2), "\n", TokenType.LINE_BREAK),
                new Token(makeSpan(22, 1, 2, 1), "56", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testMathExpression() {
        createStreams("5*(1^2-45)/10+1.01");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(1, 1, 0, 1), "*", TokenType.MULTIPLY),
                new Token(makeSpan(2, 1, 0, 2), "(", TokenType.L_PAREN),
                new Token(makeSpan(3, 1, 0, 3), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(4, 1, 0, 4), "^", TokenType.POW),
                new Token(makeSpan(5, 1, 0, 5), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 1, 0, 6), "-", TokenType.MINUS),
                new Token(makeSpan(7, 1, 0, 7), "45", TokenType.LONG_LITERAL),
                new Token(makeSpan(9, 1, 0, 9), ")", TokenType.R_PAREN),
                new Token(makeSpan(10, 1, 0, 10), "/", TokenType.DIVIDE),
                new Token(makeSpan(11, 1, 0, 11), "10", TokenType.LONG_LITERAL),
                new Token(makeSpan(13, 1, 0, 13), "+", TokenType.PLUS),
                new Token(makeSpan(14, 1, 0, 14), "1.01", TokenType.DOUBLE_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testIfStatement() {
        createStreams("if (1 == 2) { 3 }");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 1, 0, 3), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 1, 0, 4), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 1, 0, 6), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 1, 0, 9), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 1, 0, 10), ")", TokenType.R_PAREN),
                new Token(makeSpan(12, 1, 0, 12), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(14, 1, 0, 14), "3", TokenType.LONG_LITERAL),
                new Token(makeSpan(16, 1, 0, 16), "}", TokenType.R_CURLY_PAREN)
        ), tokensIntoList());
        assertStreamsEmpty();

        createStreams("if (1 == 2) { 35.45 } else 5 == 5");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 1, 0, 3), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 1, 0, 4), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 1, 0, 6), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 1, 0, 9), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 1, 0, 10), ")", TokenType.R_PAREN),
                new Token(makeSpan(12, 1, 0, 12), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(14, 1, 0, 14), "35.45", TokenType.DOUBLE_LITERAL),
                new Token(makeSpan(20, 1, 0, 20), "}", TokenType.R_CURLY_PAREN),
                new Token(makeSpan(22, 1, 0, 22), "else", TokenType.KEYWORD),
                new Token(makeSpan(27, 1, 0, 27), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(29, 1, 0, 29), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(32, 1, 0, 32), "5", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();

        createStreams("if (1 == 2) 3 else if (a) 5");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1, 0, 0), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 1, 0, 3), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 1, 0, 4), "1", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 1, 0, 6), "==", TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 1, 0, 9), "2", TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 1, 0, 10), ")", TokenType.R_PAREN),
                new Token(makeSpan(12, 1, 0, 12), "3", TokenType.LONG_LITERAL),
                new Token(makeSpan(14, 1, 0, 14), "else", TokenType.KEYWORD),
                new Token(makeSpan(19, 1, 0, 19), "if", TokenType.KEYWORD),
                new Token(makeSpan(22, 1, 0, 22), "(", TokenType.L_PAREN),
                new Token(makeSpan(23, 1, 0, 23), "a", TokenType.IDENTIFIER),
                new Token(makeSpan(24, 1, 0, 24), ")", TokenType.R_PAREN),
                new Token(makeSpan(26, 1, 0, 26), "5", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    private static List<Token> tokenList(Token... tokens) {
        List<Token> list = new ArrayList<>(Arrays.asList(tokens));

        var lastToken = list.get(list.size() - 1);
        var lastSpan = lastToken.span();
        var line = lastSpan.getLine() + countLineBreaks(lastSpan.getSource(), lastSpan.getOffset());
        var column = lastSpan.getSource().length - CharArrayUtils.findLineStart(lastSpan.getSource(), lastSpan.getSource().length - 1);

        list.add(new Token(new Span(lastSpan.getSource(), lastSpan.getSource().length, lastSpan.getSource().length + 1, line, column), "", TokenType.EOF));
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
