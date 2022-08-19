package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class LongLiteralExpression extends Expression implements IDeclarationReference {

    private final long value;

    public LongLiteralExpression(ISpan span, long value) {
        super(span);
        this.value = value;
    }

    public long getValue() {
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

        return this.value == ((LongLiteralExpression) o).value;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (this.value ^ (this.value >>> 32));
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(Long.toString(this.value)).append("L");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
