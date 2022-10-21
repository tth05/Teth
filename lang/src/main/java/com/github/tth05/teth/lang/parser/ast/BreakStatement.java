package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public final class BreakStatement extends Statement implements IDeclarationReference {

    public BreakStatement(Span span) {
        super(span);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("BreakStatement {}");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakStatement;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
