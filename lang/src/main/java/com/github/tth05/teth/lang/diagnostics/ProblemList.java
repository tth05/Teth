package com.github.tth05.teth.lang.diagnostics;

import java.util.ArrayList;
import java.util.Arrays;

public class ProblemList extends ArrayList<Problem> {

    public String prettyPrint(boolean useAnsiColors) {
        var builder = new StringBuilder();

        for (var problem : this) {
            builder.append(problem.prettyPrint(useAnsiColors)).append("\n");
        }

        return builder.toString();
    }

    public static ProblemList of(Problem... problems) {
        var list = new ProblemList();
        list.addAll(Arrays.asList(problems));
        return list;
    }
}
