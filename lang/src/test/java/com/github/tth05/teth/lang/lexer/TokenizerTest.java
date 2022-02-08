package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.stream.CharStream;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    @Test
    public void testNumber() {
        var tokens = new Tokenizer(CharStream.fromString("12345")).tokenize();
        assertIterableEquals(tokenList(new Token("12345", TokenType.NUMBER)), tokens);
    }

    @Test
    public void testIdentifier() {
        var tokens = new Tokenizer(CharStream.fromString("anIdentifier")).tokenize();
        assertIterableEquals(tokenList(new Token("anIdentifier", TokenType.IDENTIFIER)), tokens);
    }

    @Test
    public void testIdentifierStartsWithNumber() {
        assertThrows(UnexpectedCharException.class, () -> new Tokenizer(CharStream.fromString("123anIdentifier")).tokenize());
    }

    @Test
    public void testInvalidChars() {
        assertThrows(UnexpectedCharException.class, () -> new Tokenizer(CharStream.fromString("ä")).tokenize());
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

    private List<Token> tokenList(Token ... tokens) {
        List<Token> list = new ArrayList<>(Arrays.asList(tokens));
        list.add(new Token("", TokenType.EOF));
        return list;
    }
}
