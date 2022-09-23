package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.span.ArrayListWithSpan;

import java.util.Collections;

public class StatementList extends ArrayListWithSpan<Statement> {

    public StatementList() {
    }

    public static StatementList of(Statement... statements) {
        var list = new StatementList();
        Collections.addAll(list, statements);
        return list;
    }
}
