package com.github.tth05.teth.lang.diagnostics;

import java.util.ArrayList;
import java.util.Arrays;

public class ProblemList extends ArrayList<Problem> {

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
