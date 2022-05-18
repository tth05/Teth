package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.stream.CharStream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractInterpreterTest {

    protected TokenStream tokenStream;
    protected CharStream charStream;

    protected SourceFileUnit unit;

    protected void createAST(String str) {
        createStreams(str);
        this.unit = Parser.from(this.tokenStream);
    }

    private void createStreams(String str) {
        this.charStream = CharStream.fromString(str);
        this.tokenStream = Tokenizer.streamOf(this.charStream);
    }

    protected void assertStreamsEmpty() {
        assertTrue(this.charStream.isEmpty());
        assertTrue(this.tokenStream.isEmpty());
    }
}
