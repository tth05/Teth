package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public final class BooleanLiteralExpression extends Expression implements IDeclarationReference {

    private final boolean value;

    public BooleanLiteralExpression(Span span, boolean value) {
        super(span);
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
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

        BooleanLiteralExpression that = (BooleanLiteralExpression) o;

        return this.value == that.value;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.value + "");
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(this.value);
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
