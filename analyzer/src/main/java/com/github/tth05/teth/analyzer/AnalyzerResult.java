package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.diagnostics.ProblemList;

public class AnalyzerResult {

    private final String moduleName;
    private final ProblemList problems;

    public AnalyzerResult(String moduleName, ProblemList problems) {
        this.moduleName = moduleName;
        this.problems = problems;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public boolean hasProblems() {
        return !this.problems.isEmpty();
    }

    public ProblemList getProblems() {
        return this.problems;
    }
}
