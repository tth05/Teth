package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class VariableAssignmentExpression extends Expression {

    private final Expression targetExpr;
    private final Expression expr;

    public VariableAssignmentExpression(ISpan span, Expression targetExpr, Expression expr) {
        super(span);
        if (!(targetExpr instanceof IAssignmentTarget))
            throw new IllegalArgumentException("targetExpr must be an IAssignmentTarget");

        this.targetExpr = targetExpr;
        this.expr = expr;
    }

    public Expression getTargetExpr() {
        return this.targetExpr;
    }

    public Expression getExpr() {
        return this.expr;
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

        VariableAssignmentExpression that = (VariableAssignmentExpression) o;

        if (!this.targetExpr.equals(that.targetExpr))
            return false;
        return this.expr.equals(that.expr);
    }

    @Override
    public int hashCode() {
        int result = this.targetExpr.hashCode();
        result = 31 * result + this.expr.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("VariableAssignmentExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("target");
        this.targetExpr.dump(builder);
        builder.newLine().appendAttribute("expr");
        this.expr.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
