package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.source.InMemorySource;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractAnalyzerTest {

    protected List<AST> asts;

    protected Analyzer analyzer;

    protected ProblemList analyze(String main) {
        var problems = analyze(main, new Source[0]);
        return problems.get(0).getProblems();
    }

    protected List<AnalyzerResult> analyze(String main, Source... others) {
        createASTs(main, others);
        assertStreamsEmpty();

        this.analyzer = new Analyzer(this.asts.stream().map(AST::unit).toList());
        return this.analyzer.analyze();
    }

    private void createASTs(String main, Source... others) {
        this.asts = Stream.concat(Stream.of(new Source("main", main)), Arrays.stream(others)).map(this::createAST).toList();
    }

    private AST createAST(Source source) {
        var charStream = CharStream.fromSource(new InMemorySource(source.name(), source.content()));
        var tokenizerResult = Tokenizer.tokenize(charStream);
        if (tokenizerResult.hasProblems())
            throw new RuntimeException("Tokenizer failed\n" + tokenizerResult.getProblems().prettyPrint(true));
        var tokenStream = tokenizerResult.getTokenStream();

        var parserResult = Parser.parse(tokenStream);
        if (parserResult.hasProblems())
            throw new RuntimeException("Parser failed\n" + parserResult.getProblems().prettyPrint(true));
        return new AST(tokenStream, charStream, parserResult.getUnit());
    }

    protected void assertStreamsEmpty() {
        for (AST ast : this.asts) {
            assertTrue(ast.charStream.isEmpty());
            assertTrue(ast.tokenStream.isEmpty());
        }
    }

    protected record Source(String name, String content) {}

    protected record AST(TokenStream tokenStream, CharStream charStream, SourceFileUnit unit) {}
}
