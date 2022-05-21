package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.stream.CharStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractInterpreterTest {

    private final ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
    private final PrintStream originalSystemOut = System.out;

    protected TokenStream tokenStream;
    protected CharStream charStream;

    protected SourceFileUnit unit;

    @BeforeEach
    public void hookSystemOut() {
        System.setOut(new PrintStream(this.tempOutputStream));
    }

    @AfterEach
    public void restoreSystemOut() {
        System.setOut(this.originalSystemOut);
    }

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

    protected String getSystemOutput() {
        return this.tempOutputStream.toString();
    }
}
