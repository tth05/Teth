package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.Problem;

import java.util.List;

public class TokenizerResult {

    private final TokenStream stream;
    private final List<Problem> problems;

    public TokenizerResult(TokenStream stream) {
        this(stream, null);
    }

    public TokenizerResult(TokenStream stream, List<Problem> problems) {
        this.stream = stream;
        this.problems = problems;
    }

    public TokenStream getTokenStream() {
        return this.stream;
    }

    public List<Problem> getProblems() {
        return this.problems;
    }

    public boolean logProblems() {
        var problems = getProblems();
        if (problems == null)
            return false;

        System.err.println("Tokenizer problems:");
        for (var problem : problems) {
            System.out.println(problem.prettyPrint());
        }

        return true;

    }
}
