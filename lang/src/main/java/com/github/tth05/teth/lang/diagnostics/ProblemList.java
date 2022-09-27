package com.github.tth05.teth.lang.diagnostics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProblemList {

    private final List<Problem> problems = new ArrayList<>();

    public void add(Problem problem) {
        for (var other : this.problems) {
            if (other.span().offset() == problem.span().offset())
                return;
        }

        this.problems.add(problem);
    }

    public Problem get(int index) {
        return this.problems.get(index);
    }

    public void merge(ProblemList other) {
        if (other.isEmpty())
            return;

        for (var problem : other.problems)
            add(problem);
    }

    public int size() {
        return this.problems.size();
    }

    public boolean isEmpty() {
        return this.problems.isEmpty();
    }

    public String prettyPrint(boolean useAnsiColors) {
        var builder = new StringBuilder();

        for (int i = 0; i < this.problems.size(); i++) {
            var problem = this.problems.get(i);
            builder.append(problem.prettyPrint(useAnsiColors));
            if (i < this.problems.size() - 1)
                builder.append('\n');
        }

        return builder.toString();
    }

    public static ProblemList of(Problem... problems) {
        var list = new ProblemList();
        list.problems.addAll(Arrays.asList(problems));
        return list;
    }
}
