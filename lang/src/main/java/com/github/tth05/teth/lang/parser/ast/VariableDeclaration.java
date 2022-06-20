package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;

public class VariableDeclaration extends Statement implements IVariableDeclaration {

    private final IdentifierExpression nameExpr;
    private final Expression initializer;

    private TypeExpression type;

    public VariableDeclaration(ISpan span, TypeExpression type, IdentifierExpression nameExpr, Expression initializer) {
        super(span);
        this.type = type;
        this.nameExpr = nameExpr;
        this.initializer = initializer;
    }

    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    @Override
    public void setInferredType(Type type) {
        if (this.type != null)
            throw new UnsupportedOperationException("Cannot override existing type");

        this.type = new TypeExpression(this.nameExpr.getSpan(), type);
    }

    public TypeExpression getTypeExpr() {
        return this.type;
    }

    public Expression getInitializerExpr() {
        return this.initializer;
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

        VariableDeclaration that = (VariableDeclaration) o;

        if (!this.nameExpr.equals(that.nameExpr))
            return false;
        if (!this.initializer.equals(that.initializer))
            return false;
        return Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        int result = this.nameExpr.hashCode();
        result = 31 * result + this.initializer.hashCode();
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("VariableDeclaration {").newLine();
        builder.startBlock();
        builder.appendAttribute("type", this.type != null ? this.type.toString() : "<no type>").newLine();
        builder.appendAttribute("name");
        this.nameExpr.dump(builder);
        builder.newLine().appendAttribute("expression");
        this.initializer.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
