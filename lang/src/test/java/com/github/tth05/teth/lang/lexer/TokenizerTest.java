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
        assertIterableEquals(tokenList(new Token(makeSpan(0, 5),  TokenType.LONG_LITERAL)), tokensIntoList());

        createStreams("12345.12345");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 11),  TokenType.DOUBLE_LITERAL)), tokensIntoList());
    }

    @Test
    public void testNegativeNumber() {
        createStreams("-12345");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), TokenType.MINUS),
                new Token(makeSpan(1, 6), TokenType.LONG_LITERAL)
        ), tokensIntoList());

        createStreams("-12345.54");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), TokenType.MINUS),
                new Token(makeSpan(1, 9), TokenType.DOUBLE_LITERAL)
        ), tokensIntoList());
    }

    @Test
    public void testInvalidNumber() {
        assertThrows(Exception.class, () -> createStreams("123anIdentifier"));
    }

    @Test
    public void testSimpleString() {
        createStreams("\"a cool string\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 15), TokenType.STRING_LITERAL)), tokensIntoList());

        createStreams("\"teth_is_great!\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 16), TokenType.STRING_LITERAL)), tokensIntoList());

        createStreams("\"teth\\\"hi\\\"1\\\\\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 15), TokenType.STRING_LITERAL)), tokensIntoList());

        createStreams("\"hi \\{t}\"");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 9), TokenType.STRING_LITERAL)), tokensIntoList());

        createStreams("\"hi {5 + 5} 1\"");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 4), TokenType.STRING_LITERAL),
                new Token(makeSpan(4, 5), TokenType.STRING_LITERAL_CODE_START),
                new Token(makeSpan(5, 6), TokenType.LONG_LITERAL),
                new Token(makeSpan(7, 8), TokenType.PLUS),
                new Token(makeSpan(9, 10), TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), TokenType.STRING_LITERAL_CODE_END),
                new Token(makeSpan(11, 14), TokenType.STRING_LITERAL)
        ), tokensIntoList());
    }

    @Test
    public void testUnclosedString() {
        assertThrows(Exception.class, () -> createStreams("\"123anIdentifier"));
        assertThrows(Exception.class, () -> createStreams("\"123anIdentifier\n"));
    }

    @Test
    public void testBoolean() {
        createStreams("true");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 4), TokenType.BOOLEAN_LITERAL)), tokensIntoList());

        createStreams("false");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 5), TokenType.BOOLEAN_LITERAL)), tokensIntoList());
    }

    @Test
    public void testIdentifier() {
        createStreams("anIdentifier");
        assertIterableEquals(tokenList(new Token(makeSpan(0, 12), TokenType.IDENTIFIER)), tokensIntoList());
    }

    @Test
    public void testListLiteral() {
        createStreams("[5 + 5, true, \"str\"]");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), TokenType.L_SQUARE_BRACKET),
                new Token(makeSpan(1, 2), TokenType.LONG_LITERAL),
                new Token(makeSpan(3, 4), TokenType.PLUS),
                new Token(makeSpan(5, 6), TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 7), TokenType.COMMA),
                new Token(makeSpan(8, 12), TokenType.BOOLEAN_LITERAL),
                new Token(makeSpan(12, 13), TokenType.COMMA),
                new Token(makeSpan(14, 19), TokenType.STRING_LITERAL),
                new Token(makeSpan(19, 20), TokenType.R_SQUARE_BRACKET)
        ), tokensIntoList());
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
        createStreams("let anIdentifier: type = null");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 3), TokenType.KEYWORD_LET),
                new Token(makeSpan(4, 16), TokenType.IDENTIFIER),
                new Token(makeSpan(16, 17), TokenType.COLON),
                new Token(makeSpan(18, 22), TokenType.IDENTIFIER),
                new Token(makeSpan(23, 24), TokenType.EQUAL),
                new Token(makeSpan(25, 29), TokenType.KEYWORD_NULL)
        ), tokensIntoList());
    }

    @Test
    public void testAssignMultiline() {
        createStreams("let anIdentifier\n =\n 56");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 3), TokenType.KEYWORD_LET),
                new Token(makeSpan(4, 16), TokenType.IDENTIFIER),
                new Token(makeSpan(16, 17), TokenType.LINE_BREAK),
                new Token(makeSpan(18, 19), TokenType.EQUAL),
                new Token(makeSpan(19, 20), TokenType.LINE_BREAK),
                new Token(makeSpan(21, 23), TokenType.LONG_LITERAL)
        ), tokensIntoList());
    }

    @Test
    public void testMathExpression() {
        createStreams("5*(1^2-45)/10+1.01");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), TokenType.LONG_LITERAL),
                new Token(makeSpan(1, 2), TokenType.MULTIPLY),
                new Token(makeSpan(2, 3), TokenType.L_PAREN),
                new Token(makeSpan(3, 4), TokenType.LONG_LITERAL),
                new Token(makeSpan(4, 5), TokenType.POW),
                new Token(makeSpan(5, 6), TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 7), TokenType.MINUS),
                new Token(makeSpan(7, 9), TokenType.LONG_LITERAL),
                new Token(makeSpan(9, 10), TokenType.R_PAREN),
                new Token(makeSpan(10, 11), TokenType.SLASH),
                new Token(makeSpan(11, 13), TokenType.LONG_LITERAL),
                new Token(makeSpan(13, 14), TokenType.PLUS),
                new Token(makeSpan(14, 18), TokenType.DOUBLE_LITERAL)
        ), tokensIntoList());
    }

    @Test
    public void testIfStatement() {
        createStreams("if (1 == 2) { 3 }");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), TokenType.KEYWORD_IF),
                new Token(makeSpan(3, 4), TokenType.L_PAREN),
                new Token(makeSpan(4, 5), TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 8), TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 10), TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), TokenType.R_PAREN),
                new Token(makeSpan(12, 13), TokenType.L_CURLY_PAREN),
                new Token(makeSpan(14, 15), TokenType.LONG_LITERAL),
                new Token(makeSpan(16, 17), TokenType.R_CURLY_PAREN)
        ), tokensIntoList());

        createStreams("if (a && b || c) { 3 }");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), TokenType.KEYWORD_IF),
                new Token(makeSpan(3, 4), TokenType.L_PAREN),
                new Token(makeSpan(4, 5), TokenType.IDENTIFIER),
                new Token(makeSpan(6, 8), TokenType.AMPERSAND_AMPERSAND),
                new Token(makeSpan(9, 10), TokenType.IDENTIFIER),
                new Token(makeSpan(11, 13), TokenType.PIPE_PIPE),
                new Token(makeSpan(14, 15), TokenType.IDENTIFIER),
                new Token(makeSpan(15, 16), TokenType.R_PAREN),
                new Token(makeSpan(17, 18), TokenType.L_CURLY_PAREN),
                new Token(makeSpan(19, 20), TokenType.LONG_LITERAL),
                new Token(makeSpan(21, 22), TokenType.R_CURLY_PAREN)
        ), tokensIntoList());

        createStreams("if (1 == 2) { 35.45 } else 5 == 5");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), TokenType.KEYWORD_IF),
                new Token(makeSpan(3, 4), TokenType.L_PAREN),
                new Token(makeSpan(4, 5), TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 8), TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 10), TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), TokenType.R_PAREN),
                new Token(makeSpan(12, 13), TokenType.L_CURLY_PAREN),
                new Token(makeSpan(14, 19), TokenType.DOUBLE_LITERAL),
                new Token(makeSpan(20, 21), TokenType.R_CURLY_PAREN),
                new Token(makeSpan(22, 26), TokenType.KEYWORD_ELSE),
                new Token(makeSpan(27, 28), TokenType.LONG_LITERAL),
                new Token(makeSpan(29, 31), TokenType.EQUAL_EQUAL),
                new Token(makeSpan(32, 33), TokenType.LONG_LITERAL)
        ), tokensIntoList());

        createStreams("if (1 == 2) 3 else if (a) 5");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 2), TokenType.KEYWORD_IF),
                new Token(makeSpan(3, 4), TokenType.L_PAREN),
                new Token(makeSpan(4, 5), TokenType.LONG_LITERAL),
                new Token(makeSpan(6, 8), TokenType.EQUAL_EQUAL),
                new Token(makeSpan(9, 10), TokenType.LONG_LITERAL),
                new Token(makeSpan(10, 11), TokenType.R_PAREN),
                new Token(makeSpan(12, 13), TokenType.LONG_LITERAL),
                new Token(makeSpan(14, 18), TokenType.KEYWORD_ELSE),
                new Token(makeSpan(19, 21), TokenType.KEYWORD_IF),
                new Token(makeSpan(22, 23), TokenType.L_PAREN),
                new Token(makeSpan(23, 24), TokenType.IDENTIFIER),
                new Token(makeSpan(24, 25), TokenType.R_PAREN),
                new Token(makeSpan(26, 27), TokenType.LONG_LITERAL)
        ), tokensIntoList());
    }

    @Test
    public void testLoop() {
        createStreams("loop {}");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 4), TokenType.KEYWORD_LOOP),
                new Token(makeSpan(5, 6), TokenType.L_CURLY_PAREN),
                new Token(makeSpan(6, 7), TokenType.R_CURLY_PAREN)
        ), tokensIntoList());
    }

    @Test
    public void testStruct() {
        createStreams("struct b {} let a = new b()");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 6), TokenType.KEYWORD_STRUCT),
                new Token(makeSpan(7, 8), TokenType.IDENTIFIER),
                new Token(makeSpan(9, 10), TokenType.L_CURLY_PAREN),
                new Token(makeSpan(10, 11), TokenType.R_CURLY_PAREN),
                new Token(makeSpan(12, 15), TokenType.KEYWORD_LET),
                new Token(makeSpan(16, 17), TokenType.IDENTIFIER),
                new Token(makeSpan(18, 19), TokenType.EQUAL),
                new Token(makeSpan(20, 23), TokenType.KEYWORD_NEW),
                new Token(makeSpan(24, 25), TokenType.IDENTIFIER),
                new Token(makeSpan(25, 26), TokenType.L_PAREN),
                new Token(makeSpan(26, 27), TokenType.R_PAREN)
        ), tokensIntoList());
    }

    @Test
    public void testIntrinsic() {
        createStreams("intrinsic");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 9), TokenType.KEYWORD_INTRINSIC)
        ), tokensIntoList());
    }

    @Test
    public void testInvocation() {
        createStreams("a<|long, double>()");
        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 1), TokenType.IDENTIFIER),
                new Token(makeSpan(1, 3), TokenType.LESS_PIPE),
                new Token(makeSpan(3, 7), TokenType.IDENTIFIER),
                new Token(makeSpan(7, 8), TokenType.COMMA),
                new Token(makeSpan(9, 15), TokenType.IDENTIFIER),
                new Token(makeSpan(15, 16), TokenType.GREATER),
                new Token(makeSpan(16, 17), TokenType.L_PAREN),
                new Token(makeSpan(17, 18), TokenType.R_PAREN)
        ), tokensIntoList());
    }

    @Test
    public void testComments() {
        createStreams("""
                // Hello
                5 // Test
                // World
                /*
                Multi
                line
                */
                6 /*7*/ + /*hi*/8""");

        assertIterableEquals(tokenList(
                new Token(makeSpan(0, 8), TokenType.COMMENT),
                new Token(makeSpan(8, 9), TokenType.LINE_BREAK),
                new Token(makeSpan(9, 10), TokenType.LONG_LITERAL),
                new Token(makeSpan(11, 18), TokenType.COMMENT),
                new Token(makeSpan(18, 19), TokenType.LINE_BREAK),
                new Token(makeSpan(19, 27), TokenType.COMMENT),
                new Token(makeSpan(27, 28), TokenType.LINE_BREAK),
                new Token(makeSpan(28, 44), TokenType.COMMENT),
                new Token(makeSpan(44, 45), TokenType.LINE_BREAK),
                new Token(makeSpan(45, 46), TokenType.LONG_LITERAL),
                new Token(makeSpan(47, 52), TokenType.COMMENT),
                new Token(makeSpan(53, 54), TokenType.PLUS),
                new Token(makeSpan(55, 61), TokenType.COMMENT),
                new Token(makeSpan(61, 62), TokenType.LONG_LITERAL)
        ), tokensIntoList());
    }

    private static List<Token> tokenList(Token... tokens) {
        List<Token> list = new ArrayList<>(Arrays.asList(tokens));

        var lastToken = list.get(list.size() - 1);
        var lastSpan = lastToken.span();

        var length = CharArrayUtils.trimEnd(lastSpan.source().getContents());
        list.add(new Token(new Span(lastSpan.source(), length, length + 1), TokenType.EOF));
        return list;
    }
}
