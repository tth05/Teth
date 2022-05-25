package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class VariableAssignmentExpression extends Expression {

    private final String target;
    private final Expression expr;

    public VariableAssignmentExpression(ISpan span, String target, Expression expr) {
        super(span);
        this.target = target;
        this.expr = expr;
    }

    public String getTarget() {
        return this.target;
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

        if (!this.target.equals(that.target))
            return false;
        return this.expr.equals(that.expr);
    }

    @Override
    public int hashCode() {
        int result = this.target.hashCode();
        result = 31 * result + this.expr.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("VariableAssignmentExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("target", this.target).newLine();
        builder.appendAttribute("expr");
        this.expr.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
