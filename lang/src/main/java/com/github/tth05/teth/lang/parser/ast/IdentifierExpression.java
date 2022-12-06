package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;


public final class IdentifierExpression extends Expression implements IAssignmentTarget {

    public IdentifierExpression(Span span) {
        super(span);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return this.getSpan().textEquals(((IdentifierExpression) o).getSpan());
    }

    @Override
    public int hashCode() {
        return this.getSpan().getText().hashCode();
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.getSpan() != null ? this.getSpan().getText() : "???");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
