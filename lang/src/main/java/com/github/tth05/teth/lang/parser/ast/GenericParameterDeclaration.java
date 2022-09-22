package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;


public class GenericParameterDeclaration extends Statement implements IHasName {

    private final IdentifierExpression nameExpr;

    public GenericParameterDeclaration(Span span, String name) {
        super(span);
        this.nameExpr = new IdentifierExpression(span, name);
    }

    @Override
    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
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

        return Objects.equals(this.nameExpr, ((GenericParameterDeclaration) o).nameExpr);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.nameExpr.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        this.nameExpr.dump(builder);
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
