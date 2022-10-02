package com.github.tth05.teth.lang;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenStream;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.source.ISource;
import com.github.tth05.teth.lang.source.InMemorySource;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.stream.CharStream;

import java.util.List;

public abstract class AbstractTokenizerTest {

    protected TokenStream tokenStream;
    protected CharStream charStream;
    private ISource source;

    protected void createStreams(String str) {
        this.source = new InMemorySource("main", str);
        this.charStream = CharStream.fromSource(this.source);
        var tokenizerResult = Tokenizer.tokenize(this.charStream);
        if (tokenizerResult.hasProblems())
            throw new RuntimeException("Tokenizer failed\n" + tokenizerResult.getProblems().prettyPrint(false));
        this.tokenStream = tokenizerResult.getTokenStream();
    }

    protected List<Token> tokensIntoList() {
        return this.tokenStream.getTokens().stream().filter(t -> t.type() != TokenType.WHITESPACE).toList();
    }

    protected Span makeSpan(int offset, int offsetEnd) {
        return new Span(this.source, offset, offsetEnd);
    }
}
