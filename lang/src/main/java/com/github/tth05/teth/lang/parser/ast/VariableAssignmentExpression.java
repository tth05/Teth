package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class VariableAssignmentExpression extends Expression {

    private final IdentifierExpression targetNameExpr;
    private final Expression expr;

    public VariableAssignmentExpression(ISpan span, IdentifierExpression targetNameExpr, Expression expr) {
        super(span);
        this.targetNameExpr = targetNameExpr;
        this.expr = expr;
    }

    public IdentifierExpression getTargetNameExpr() {
        return this.targetNameExpr;
    }

    public Expression getExpr() {
        return this.expr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VariableAssignmentExpression that = (VariableAssignmentExpression) o;

        if (!this.targetNameExpr.equals(that.targetNameExpr))
            return false;
        return this.expr.equals(that.expr);
    }

    @Override
    public int hashCode() {
        int result = this.targetNameExpr.hashCode();
        result = 31 * result + this.expr.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("VariableAssignmentExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("target");
        this.targetNameExpr.dump(builder);
        builder.newLine().appendAttribute("expr");
        this.expr.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}