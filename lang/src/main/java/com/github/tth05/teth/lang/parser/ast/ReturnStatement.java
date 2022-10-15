package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;

public final class ReturnStatement extends Statement {

    private final Expression valueExpr;

    public ReturnStatement(Span span, Expression valueExpr) {
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
        if (this.valueExpr != null)
            this.valueExpr.dump(builder);
        else
            builder.append("<none>");
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ReturnStatement that = (ReturnStatement) o;

        return Objects.equals(this.valueExpr, that.valueExpr);
    }

    @Override
    public int hashCode() {
        return this.valueExpr != null ? this.valueExpr.hashCode() : 0;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
