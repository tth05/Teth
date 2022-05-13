package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class ElseStatement extends Statement {

    private final BlockStatement elseStatement;

    public ElseStatement(BlockStatement elseStatement) {
        this.elseStatement = elseStatement;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("Else {").newLine();
        builder.startBlock();
        builder.appendAttribute("body");
        this.elseStatement.dump(builder);
        builder.endBlock();
        builder.newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ElseStatement that = (ElseStatement) o;

        return this.elseStatement.equals(that.elseStatement);
    }

    @Override
    public int hashCode() {
        return this.elseStatement.hashCode();
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
