package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;


public class IdentifierExpression extends Expression implements IDeclarationReference{

    private final String value;

    public IdentifierExpression(ISpan span, String value) {
        super(span);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public IdentifierExpression getReferenceNameExpr() {
        return this;
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

        return Objects.equals(this.value, ((IdentifierExpression) o).value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.value.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.value);
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
