package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class GarbageExpression extends Expression {

    public GarbageExpression(ISpan span) {
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
