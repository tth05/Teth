package com.github.tth05.teth.lang;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTokenizerTest {

    protected TokenStream tokenStream;
    protected CharStream charStream;


    protected void createStreams(String str) {
        this.charStream = CharStream.fromString(str);
        this.tokenStream = Tokenizer.streamOf(this.charStream);
    }

    protected List<Token> tokensIntoList() {
        List<Token> tokens = new ArrayList<>();
        while (!this.tokenStream.isEmpty())
            tokens.add(this.tokenStream.consume());

        return tokens;
    }

    public void assertStreamsEmpty() {
        assertTrue(this.charStream.isEmpty());
        assertTrue(this.tokenStream.isEmpty());
    }
}
