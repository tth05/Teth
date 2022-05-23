package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class ReturnStatement extends Statement {

    private final Expression valueExpr;

    public ReturnStatement(Expression valueExpr) {
        this.valueExpr = valueExpr;
    }

    public Expression getValueExpr() {
        return this.valueExpr;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("Return {").newLine().startBlock();
        builder.appendAttribute("value");
        this.valueExpr.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ReturnStatement that = (ReturnStatement) o;

        return this.valueExpr.equals(that.valueExpr);
    }

    @Override
    public int hashCode() {
        return this.valueExpr.hashCode();
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
