package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.source.ISource;

import java.io.PrintStream;

public class ParserResult {

    private final ISource source;
    private final SourceFileUnit unit;
    private final ProblemList problems;

    public ParserResult(ISource source, SourceFileUnit unit) {
        this(source, unit, ProblemList.of());
    }

    public ParserResult(ISource source, SourceFileUnit unit, ProblemList problems) {
        this.source = source;
        this.unit = unit;
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

    public SourceFileUnit getUnit() {
        return this.unit;
    }

    public boolean hasProblems() {
        return !this.problems.isEmpty();
    }

    public ProblemList getProblems() {
        return this.problems;
    }
}
