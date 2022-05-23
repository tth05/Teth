package com.github.tth05.teth.lang;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTokenizerTest {

    protected TokenStream tokenStream;
    protected CharStream charStream;
    private char[] source;

    protected void createStreams(String str) {
        this.source = str.toCharArray();
        this.charStream = CharStream.fromString(str);
        var tokenizerResult = Tokenizer.streamOf(this.charStream);
        if (tokenizerResult.getProblems() != null)
            throw new RuntimeException("Tokenizer failed");
        this.tokenStream = tokenizerResult.getTokenStream();
    }

    protected List<Token> tokensIntoList() {
        List<Token> tokens = new ArrayList<>();
        while (!this.tokenStream.isEmpty())
            tokens.add(this.tokenStream.consume());

        return tokens;
    }

    protected Span makeSpan(int offset, int line, int column) {
        return new Span(this.source, offset, line, column);
    }

    public void assertStreamsEmpty() {
        assertTrue(this.charStream.isEmpty());
        assertTrue(this.tokenStream.isEmpty());
    }
}
