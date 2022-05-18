package com.github.tth05.teth.lang.ast;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Objects;

public class IfStatement extends Statement {

    private final Expression condition;
    private final BlockStatement body;
    private final Statement elseStatement;

    public IfStatement(Expression condition, BlockStatement body, Statement elseStatement) {
        this.condition = condition;
        this.body = body;
        this.elseStatement = elseStatement;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("If {").newLine();
        builder.startBlock();
        builder.appendAttribute("expression");
        this.condition.dump(builder);
        builder.newLine().appendAttribute("body");
        this.body.dump(builder);
        if (this.elseStatement != null) {
            builder.newLine().appendAttribute("else");
            this.elseStatement.dump(builder);
        }
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IfStatement that = (IfStatement) o;

        if (!this.condition.equals(that.condition))
            return false;
        if (!this.body.equals(that.body))
            return false;
        return Objects.equals(this.elseStatement, that.elseStatement);
    }

    @Override
    public int hashCode() {
        int result = this.condition.hashCode();
        result = 31 * result + this.body.hashCode();
        result = 31 * result + (this.elseStatement != null ? this.elseStatement.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
