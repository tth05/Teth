package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public final class GarbageExpression extends Expression {

    public GarbageExpression(Span span) {
        super(span);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("GarbageExpression");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    //TODO
}
