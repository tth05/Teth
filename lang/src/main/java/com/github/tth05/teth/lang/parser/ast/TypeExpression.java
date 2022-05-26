package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;


public class TypeExpression extends Expression {

    private final Type type;

    public TypeExpression(ISpan span, Type type) {
        super(span);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return Objects.equals(this.type, ((TypeExpression) o).type);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.type.toString());
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
