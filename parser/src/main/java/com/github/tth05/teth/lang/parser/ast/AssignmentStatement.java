package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class AssignmentStatement extends Statement {

    private final Expression targetExpression;
    private final Expression valueExpression;

    public AssignmentStatement(Expression targetExpression, Expression valueExpression) {
        this.targetExpression = targetExpression;
        this.valueExpression = valueExpression;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("Assignment {").newLine();
        builder.startBlock();
        builder.appendAttribute("target");
        this.targetExpression.dump(builder);
        builder.newLine().appendAttribute("expression");
        this.valueExpression.dump(builder);
        builder.endBlock();
        builder.newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AssignmentStatement that = (AssignmentStatement) o;

        if (!this.targetExpression.equals(that.targetExpression))
            return false;
        return this.valueExpression.equals(that.valueExpression);
    }

    @Override
    public int hashCode() {
        int result = this.targetExpression.hashCode();
        result = 31 * result + this.valueExpression.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
