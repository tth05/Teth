package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.lang.diagnostics.Problem;
import com.github.tth05.teth.lang.diagnostics.ProblemList;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;

public class AnalysisASTVisitor extends ASTVisitor {

    private final ProblemList problems = new ProblemList();
    private boolean errorFlag;

    public ProblemList getProblems() {
        return this.problems;
    }

    protected void report(Span span, String message) {
        if (span == null)
            return;

        this.problems.add(new Problem(span, message));
        this.errorFlag = true;
    }

    protected void clearErrorFlag() {
        this.errorFlag = false;
    }

    protected boolean hasErrorFlag() {
        return this.errorFlag;
    }
}
