package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;


public class GenericParameterDeclaration extends Statement {

    private final String name;

    public GenericParameterDeclaration(ISpan span, String name) {
        super(span);
        this.name = name;
    }

    public String getName() {
        return this.name;
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

        return Objects.equals(this.name, ((GenericParameterDeclaration) o).name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.name.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.name);
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
