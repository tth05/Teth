package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;

public class VariableDeclaration extends Statement {

    private final TypeExpression type;
    private final IdentifierExpression nameExpr;

    private final Expression expression;

    public VariableDeclaration(ISpan span, TypeExpression type, IdentifierExpression nameExpr) {
        this(span, type, nameExpr, null);
    }

    public VariableDeclaration(ISpan span, TypeExpression type, IdentifierExpression nameExpr, Expression expression) {
        super(span);
        this.type = type;
        this.nameExpr = nameExpr;
        this.expression = expression;
    }

    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    public TypeExpression getType() {
        return this.type;
    }

    public Expression getExpression() {
        return this.expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VariableDeclaration that = (VariableDeclaration) o;

        if (!this.type.equals(that.type))
            return false;
        if (!this.nameExpr.equals(that.nameExpr))
            return false;
        return Objects.equals(this.expression, that.expression);
    }

    @Override
    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.nameExpr.hashCode();
        result = 31 * result + (this.expression != null ? this.expression.hashCode() : 0);
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("VariableDeclaration {").newLine();
        builder.startBlock();
        builder.appendAttribute("type", this.type.toString()).newLine();
        builder.appendAttribute("name");
        this.nameExpr.dump(builder);
        builder.newLine().appendAttribute("expression");
        if (this.expression != null)
            this.expression.dump(builder);
        else
            builder.append("<none>");
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
