package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.ExpressionList;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FunctionInvocationExpression extends Expression {

    private final Expression target;
    private final List<TypeExpression> genericParameters;
    private final ExpressionList parameters;

    public FunctionInvocationExpression(ISpan span, Expression target, List<TypeExpression> genericParameters, ExpressionList parameters) {
        super(span);
        this.target = target;
        this.genericParameters = genericParameters != null ? Collections.unmodifiableList(genericParameters) : null;
        this.parameters = Objects.requireNonNull(parameters);
    }

    public Expression getTarget() {
        return this.target;
    }

    public List<TypeExpression> getGenericParameters() {
        return this.genericParameters;
    }

    public ExpressionList getParameters() {
        return this.parameters;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("FunctionInvocationExpression {").startBlock().newLine();
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
