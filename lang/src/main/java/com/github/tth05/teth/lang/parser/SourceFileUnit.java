package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.ArrayList;
import java.util.List;

public class SourceFileUnit {

    private final List<Statement> statements = new ArrayList<>();

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    @Override
    public String toString() {
        return "SourceFileUnit{" +
               "statements=" + this.statements +
               '}';
    }
}
