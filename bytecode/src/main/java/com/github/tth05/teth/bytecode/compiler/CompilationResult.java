package com.github.tth05.teth.bytecode.compiler;

import com.github.tth05.teth.bytecode.decoder.IInstrunction;
import com.github.tth05.teth.lang.diagnostics.ProblemList;

import java.io.PrintStream;

public class CompilationResult {

    private final IInstrunction[] instructions;
    private final ProblemList problems;

    public CompilationResult(ProblemList problems) {
        this.problems = problems;
        this.instructions = null;
    }

    public CompilationResult(IInstrunction[] instructions) {
        this.instructions = instructions;
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

    public IInstrunction[] getInstructions() {
        return this.instructions;
    }
}
