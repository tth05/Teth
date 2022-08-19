package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class StringLiteralExpression extends Expression implements IDeclarationReference {

    private final String value;

    public StringLiteralExpression(ISpan span, String value) {
        super(span);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("\"").append(this.value).append("\"");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StringLiteralExpression that = (StringLiteralExpression) o;

        return this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
