package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.diagnostics.ProblemList;

import java.io.PrintStream;

public class ParserResult {

    private final SourceFileUnit unit;
    private final ProblemList problems;

    public ParserResult(SourceFileUnit unit) {
        this(unit, ProblemList.of());
    }

    public ParserResult(SourceFileUnit unit, ProblemList problems) {
        this.unit = unit;
        this.problems = problems;
    }

    public boolean logProblems(PrintStream out, boolean useAnsiColors) {
        if (!hasProblems())
            return false;

        out.append(this.problems.prettyPrint(useAnsiColors));
        return true;
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
