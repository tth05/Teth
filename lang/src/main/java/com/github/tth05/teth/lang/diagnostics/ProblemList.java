package com.github.tth05.teth.lang.diagnostics;

import java.util.ArrayList;
import java.util.Arrays;

public class ProblemList extends ArrayList<Problem> {

    @Override
    public boolean add(Problem problem) {
        for (var other : this) {
            if (other.span().equals(problem.span()))
                return false;
        }

        return super.add(problem);
    }

    public String prettyPrint(boolean useAnsiColors) {
        var builder = new StringBuilder();

        for (int i = 0; i < this.size(); i++) {
            var problem = this.get(i);
            builder.append(problem.prettyPrint(useAnsiColors));
            if (i < this.size() - 1)
                builder.append('\n');
        }

        return builder.toString();
    }

    public static ProblemList of(Problem... problems) {
        var list = new ProblemList();
        list.addAll(Arrays.asList(problems));
        return list;
    }
}
