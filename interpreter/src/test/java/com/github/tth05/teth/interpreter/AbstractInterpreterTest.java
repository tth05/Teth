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
import java.util.Arrays;
import java.util.List;

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
        var tokenizerResult = Tokenizer.streamOf(this.charStream);
        if (tokenizerResult.getProblems() != null)
            throw new RuntimeException("Tokenizer failed");
        this.tokenStream = tokenizerResult.getTokenStream();
    }

    protected void assertStreamsEmpty() {
        assertTrue(this.charStream.isEmpty());
        assertTrue(this.tokenStream.isEmpty());
    }

    protected String getSystemOutput() {
        return this.tempOutputStream.toString();
    }

    protected List<String> getSystemOutputLines() {
        return Arrays.asList(getSystemOutput().split(System.lineSeparator()));
    }
}
