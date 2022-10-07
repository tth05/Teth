package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;

import java.io.PrintStream;

public class TokenizerResult {

    private final TokenStream stream;
    private final ProblemList problems;

    public TokenizerResult(TokenStream stream, ProblemList problems) {
        this.stream = stream;
        this.problems = problems;
    }

    public boolean logProblems(PrintStream out, boolean useAnsiColors) {
        if (!hasProblems())
            return false;

        out.append(this.problems.prettyPrint(useAnsiColors));
        return true;
    }

    public TokenStream getTokenStream() {
        return this.stream;
    }

    public boolean hasProblems() {
        return !this.problems.isEmpty();
    }

    public ProblemList getProblems() {
        return this.problems;
    }
}
