package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.UseStatement;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SourceFileUnit implements IDumpable {

    private final List<UseStatement> useStatements;
    private final StatementList statements;

    public SourceFileUnit(List<UseStatement> useStatements, StatementList statements) {
        this.statements = Objects.requireNonNull(statements);
        this.useStatements = Collections.unmodifiableList(Objects.requireNonNull(useStatements));
    }

    public List<UseStatement> getUseStatements() {
        return this.useStatements;
    }

    public StatementList getStatements() {
        return this.statements;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("SourceFileUnit {").newLine().startBlock();
        builder.appendAttribute("useStatements", this.useStatements);
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

        if (!this.useStatements.equals(that.useStatements))
            return false;
        return this.statements.equals(that.statements);
    }

    @Override
    public int hashCode() {
        int result = this.useStatements.hashCode();
        result = 31 * result + this.statements.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
