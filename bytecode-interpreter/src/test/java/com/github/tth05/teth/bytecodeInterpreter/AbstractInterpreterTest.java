package com.github.tth05.teth.bytecodeInterpreter;

import com.github.tth05.teth.bytecode.compiler.Compiler;
import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.source.InMemorySource;
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

    protected void execute(String code) {
        new Interpreter(compile(code)).execute();
    }

    protected TethProgram compile(String code) {
        createAST(code);
        var c = new Compiler();
        c.setEntryPoint(this.unit);
        var result = c.compile();
        if (result.hasProblems())
            throw new RuntimeException("Compiler failed\n" + result.getProblems().prettyPrint(true));

        return result.getProgram();
    }

    protected void createAST(String str) {
        createStreams(str);
        var parserResult = Parser.parse(this.tokenStream);
        if (parserResult.hasProblems())
            throw new RuntimeException("Parser failed\n" + parserResult.getProblems().prettyPrint(true));

        assertStreamsEmpty();
        this.unit = parserResult.getUnit();
    }

    private void createStreams(String str) {
        this.charStream = CharStream.fromSource(new InMemorySource("main", str));
        var tokenizerResult = Tokenizer.tokenize(this.charStream);
        if (tokenizerResult.hasProblems())
            throw new RuntimeException("Tokenizer failed\n" + tokenizerResult.getProblems().prettyPrint(true));
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
