package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.source.InMemorySource;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractAnalyzerTest {

    protected TokenStream tokenStream;
    protected CharStream charStream;

    protected SourceFileUnit unit;
    protected Analyzer analyzer;

    protected ProblemList analyze(String str) {
        createAST(str);
        assertStreamsEmpty();

        this.analyzer = new Analyzer(List.of(this.unit));
        return this.analyzer.analyze();
    }

    protected void createAST(String str) {
        createStreams(str);
        var parserResult = Parser.parse(this.tokenStream);
        if (parserResult.hasProblems())
            throw new RuntimeException("Parser failed\n" + parserResult.getProblems().prettyPrint(true));
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
}
