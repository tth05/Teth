package com.github.tth05.teth.bytecode.compiler;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.AnalyzerResult;
import com.github.tth05.teth.bytecode.program.TethProgram;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class CompilationResult {

    private final TethProgram program;
    private final Analyzer analyzer;
    private final List<AnalyzerResult> analyzerResults;

    public CompilationResult(Analyzer analyzer, List<AnalyzerResult> analyzerResults) {
        this.analyzer = analyzer;
        this.analyzerResults = analyzerResults;
        this.program = null;
    }

    public CompilationResult(Analyzer analyzer, TethProgram program) {
        this.analyzer = analyzer;
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

    public Analyzer getAnalyzer() {
        return this.analyzer;
    }

    public List<AnalyzerResult> getAnalyzerResults() {
        return this.analyzerResults;
    }

    public TethProgram getProgram() {
        return this.program;
    }
}
