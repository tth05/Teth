package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class BlockStatement extends Statement {

    protected final StatementList statements;

    public BlockStatement(ISpan span, StatementList statements) {
        super(span);
        this.statements = statements;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("BlockStatement {").newLine();
        builder.startBlock();
        this.statements.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    public StatementList getStatements() {
        return this.statements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BlockStatement))
            return false;

        BlockStatement that = (BlockStatement) o;

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
