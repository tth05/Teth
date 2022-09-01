package com.github.tth05.teth.lang.lexer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.source.ISource;

import java.io.PrintStream;

public class TokenizerResult {

    private final ISource source;
    private final TokenStream stream;
    private final ProblemList problems;

    public TokenizerResult(ISource source, TokenStream stream) {
        this(source, stream, ProblemList.of());
    }

    public TokenizerResult(ISource source, TokenStream stream, ProblemList problems) {
        this.source = source;
        this.stream = stream;
        this.problems = problems;
    }

    public boolean logProblems(PrintStream out, boolean useAnsiColors) {
        if (!hasProblems())
            return false;

        out.append(this.problems.prettyPrint(useAnsiColors));
        return true;
    }

    public ISource getSource() {
        return this.source;
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
