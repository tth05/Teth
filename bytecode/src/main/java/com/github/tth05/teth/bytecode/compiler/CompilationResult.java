package com.github.tth05.teth.bytecode.compiler;

import com.github.tth05.teth.analyzer.AnalyzerResult;
import com.github.tth05.teth.bytecode.program.TethProgram;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class CompilationResult {

    private final TethProgram program;
    private final List<AnalyzerResult> analyzerResults;

    public CompilationResult(List<AnalyzerResult> analyzerResults) {
        this.analyzerResults = analyzerResults;
        this.program = null;
    }

    public CompilationResult(TethProgram program) {
        this.program = program;
        this.analyzerResults = Collections.emptyList();
    }

    public boolean logProblems(PrintStream out, boolean useAnsiColors) {
        if (!hasProblems())
            return false;

        this.analyzerResults.forEach(r -> out.append(r.getProblems().prettyPrint(useAnsiColors)));
        return true;
    }

    public boolean hasProblems() {
        return this.analyzerResults.stream().anyMatch(AnalyzerResult::hasProblems);
    }

    public List<AnalyzerResult> getAnalyzerResults() {
        return this.analyzerResults;
    }

    public TethProgram getProgram() {
        return this.program;
    }
}
