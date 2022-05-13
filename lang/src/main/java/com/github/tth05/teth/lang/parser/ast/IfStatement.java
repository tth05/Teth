package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class IfStatement extends Statement {

    private final Expression condition;
    private final BlockStatement body;

    public IfStatement(Expression condition, BlockStatement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("If {").newLine();
        builder.startBlock();
        builder.appendAttribute("expression");
        this.condition.dump(builder);
        builder.newLine().appendAttribute("body");
        this.body.dump(builder);
        builder.endBlock();
        builder.newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IfStatement that = (IfStatement) o;

        if (!this.condition.equals(that.condition))
            return false;
        return this.body.equals(that.body);
    }

    @Override
    public int hashCode() {
        int result = this.condition.hashCode();
        result = 31 * result + this.body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
