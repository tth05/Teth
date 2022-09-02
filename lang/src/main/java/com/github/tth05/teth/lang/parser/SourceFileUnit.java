package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;

public class SourceFileUnit implements IDumpable {

    private final StatementList statements;

    public SourceFileUnit(StatementList statements) {
        this.statements = Objects.requireNonNull(statements);
    }

    public StatementList getStatements() {
        return this.statements;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("SourceFileUnit {").newLine().startBlock();
        builder.appendAttribute("statements");
        this.statements.dump(builder);
        builder.endBlock().newLine().append("}");
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
        return dumpToString();
    }
}
