package com.github.tth05.teth.bytecode.compiler;

import com.github.tth05.teth.bytecode.program.TethProgram;
import com.github.tth05.teth.lang.diagnostics.ProblemList;

import java.io.PrintStream;

public class CompilationResult {

    private final TethProgram program;
    private final ProblemList problems;

    public CompilationResult(ProblemList problems) {
        this.problems = problems;
        this.program = null;
    }

    public CompilationResult(TethProgram program) {
        this.program = program;
        this.problems = ProblemList.of();
    }

    public boolean logProblems(PrintStream out, boolean useAnsiColors) {
        if (!hasProblems())
            return false;

        out.append(this.problems.prettyPrint(useAnsiColors));
        return true;
    }

    public boolean hasProblems() {
        return !this.problems.isEmpty();
    }

    public ProblemList getProblems() {
        return this.problems;
    }

    public TethProgram getProgram() {
        return this.program;
    }
}
