package com.github.tth05.teth.lang.ast;

import com.github.tth05.teth.lang.parser.ExpressionList;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class FunctionInvocationExpression extends Expression {

    private final Expression target;
    private final ExpressionList parameters;

    public FunctionInvocationExpression(Expression target, ExpressionList parameters) {
        this.target = target;
        this.parameters = parameters;
    }

    public Expression getTarget() {
        return this.target;
    }

    public ExpressionList getParameters() {
        return this.parameters;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("FunctionInvocationExpression {").startBlock();
        builder.appendAttribute("target");
        this.target.dump(builder);
        builder.newLine().appendAttribute("parameters");
        this.parameters.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FunctionInvocationExpression that = (FunctionInvocationExpression) o;

        if (!this.target.equals(that.target))
            return false;
        return this.parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        int result = this.target.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }
}
