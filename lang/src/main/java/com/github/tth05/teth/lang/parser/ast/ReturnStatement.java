package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class ReturnStatement extends Statement {

    private final Expression valueExpr;

    public ReturnStatement(ISpan span, Expression valueExpr) {
        super(span);
        this.valueExpr = valueExpr;
    }

    public Expression getValueExpr() {
        return this.valueExpr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
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
