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
                new Token(makeSpan(0, 1), "[", TokenType.L_SQUARE_BRACKET),
                new Token(makeSpan(1, 2), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(3, 4), "+", TokenType.PLUS),
                new Token(makeSpan(5, 6), "5", TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 7), ",", TokenType.COMMA),
                new Token(makeSpan(8, 12), "true", TokenType.BOOLEAN_LITERAL),
                new Token(makeSpan(12, 13), ",", TokenType.COMMA),
                new Token(makeSpan(14, 19), "str", TokenType.STRING_LITERAL),
                new Token(makeSpan(19, 20), "]", TokenType.R_SQUARE_BRACKET)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvalidChars() {
        var matcher = Pattern.compile("^[a-zA-Z0-9+\\-*/^_:.,=\\t\\r\\n<>!\\s&|(){}\\[\\]]$").matcher("");
        for (int i = 32; i < 1000; i++) {
            var str = "" + (char) i;
            if (matcher.reset(str).matches())
                continue;

            assertThrows(Exception.class, () -> createStreams(str), "No exception thrown for '" + str + "' " + i);
        }
    }

    @Test
    public void testAssign() {
        createStreams("let anIdentifier: type = 56");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 3), "let", TokenType.KEYWORD),
                new Token(makeSpan(4, 16), "anIdentifier", TokenType.IDENTIFIER),
                new Token(makeSpan(16, 17), ":", TokenType.COLON),
                new Token(makeSpan(18, 22), "type", TokenType.IDENTIFIER),
                new Token(makeSpan(23, 24), "=", TokenType.EQUAL),
                new Token(makeSpan(25, 27), "56", TokenType.LONG_LITERAL)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testAssignMultiline() {
        createStreams("let anIdentifier\n =\n 56");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 3), "let", TokenType.KEYWORD),
                new Token(makeSpan(4, 16), "anIdentifier", TokenType.IDENTIFIER),
                new Token(makeSpan(16, 17), "\n", TokenType.LINE_BREAK),
                new Token(makeSpan(18, 19), "=", TokenType.EQUAL),
                new Token(makeSpan(19, 20), "\n", TokenType.LINE_BREAK),
                new Token(makeSpan(21, 23), "56", TokenType.LONG_LITERAL)
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

        createStreams("if (a && b || c) { 3 }");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), "if", TokenType.KEYWORD),
                new Token(makeSpan(3, 4), "(", TokenType.L_PAREN),
                new Token(makeSpan(4, 5), "a", TokenType.IDENTIFIER),
                new Token(makeSpan(6, 8), "&&", TokenType.AMPERSAND_AMPERSAND),
                new Token(makeSpan(9, 10), "b", TokenType.IDENTIFIER),
                new Token(makeSpan(11, 13), "||", TokenType.PIPE_PIPE),
                new Token(makeSpan(14, 15), "c", TokenType.IDENTIFIER),
                new Token(makeSpan(15, 16), ")", TokenType.R_PAREN),
                new Token(makeSpan(17, 18), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(19, 20), "3", TokenType.LONG_LITERAL),
                new Token(makeSpan(21, 22), "}", TokenType.R_CURLY_PAREN)
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

    @Test
    public void testLoop() {
        createStreams("loop {}");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 4), "loop", TokenType.KEYWORD),
                new Token(makeSpan(5, 6), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(6, 7), "}", TokenType.R_CURLY_PAREN)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testStruct() {
        createStreams("struct b {} let a = new b()");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 6), "struct", TokenType.KEYWORD),
                new Token(makeSpan(7, 8), "b", TokenType.IDENTIFIER),
                new Token(makeSpan(9, 10), "{", TokenType.L_CURLY_PAREN),
                new Token(makeSpan(10, 11), "}", TokenType.R_CURLY_PAREN),
                new Token(makeSpan(12, 15), "let", TokenType.KEYWORD),
                new Token(makeSpan(16, 17), "a", TokenType.IDENTIFIER),
                new Token(makeSpan(18, 19), "=", TokenType.EQUAL),
                new Token(makeSpan(20, 23), "new", TokenType.KEYWORD),
                new Token(makeSpan(24, 25), "b", TokenType.IDENTIFIER),
                new Token(makeSpan(25, 26), "(", TokenType.L_PAREN),
                new Token(makeSpan(26, 27), ")", TokenType.R_PAREN)
        ), tokensIntoList());
        assertStreamsEmpty();
    }

    @Test
    public void testInvocation() {
        createStreams("a<|long, double>()");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), "a", TokenType.IDENTIFIER),
                new Token(makeSpan(1, 3), "<|", TokenType.LESS_PIPE),
                new Token(makeSpan(3, 7), "long", TokenType.IDENTIFIER),
                new Token(makeSpan(7, 8), ",", TokenType.COMMA),
                new Token(makeSpan(9, 15), "double", TokenType.IDENTIFIER),
                new Token(makeSpan(15, 16), ">", TokenType.GREATER),
                new Token(makeSpan(16, 17), "(", TokenType.L_PAREN),
                new Token(makeSpan(17, 18), ")", TokenType.R_PAREN)
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
}
