package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.ArrayList;
import java.util.List;

public class SourceFileUnit {

    private final List<Statement> statements = new ArrayList<>();

    public SourceFileUnit() {}

    public SourceFileUnit(List<Statement> statements) {
        this.statements.addAll(statements);
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SourceFileUnit that = (SourceFileUnit) o;

        return this.statements.equals(that.statements);
    }

    @Override
    public int hashCode() {
        return this.statements.hashCode();
    }

    @Override
    public String toString() {
        return "SourceFileUnit{" +
               "statements=" + this.statements +
               '}';
    }
}
