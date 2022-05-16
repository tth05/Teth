package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.List;

public class SourceFileUnit implements IDumpable {

    private final StatementList statements = new StatementList();

    public SourceFileUnit(List<Statement> statements) {
        this.statements.addAll(statements);
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
    public void dump(ASTDumpBuilder builder) {
        builder.append("SourceFileUnit {").newLine();
        builder.startBlock();
        this.statements.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
