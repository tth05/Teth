package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.List;
import java.util.Objects;

public class LoopStatement extends Statement {

    private final List<VariableDeclaration> variableDeclarations;
    private final Expression condition;
    private final BlockStatement body;
    private final Statement advanceStatement;

    public LoopStatement(Span span, List<VariableDeclaration> variableDeclarations, Expression condition, BlockStatement body, Statement advanceStatement) {
        super(span);
        if (condition == null && advanceStatement != null)
            throw new IllegalArgumentException("advanceStatement cannot be set if condition is null");

        this.variableDeclarations = Objects.requireNonNull(variableDeclarations, "variableDeclarations cannot be null");
        this.condition = condition;
        this.body = body;
        this.advanceStatement = advanceStatement;
    }

    public List<VariableDeclaration> getVariableDeclarations() {
        return this.variableDeclarations;
    }

    public Expression getCondition() {
        return this.condition;
    }

    public BlockStatement getBody() {
        return this.body;
    }

    public Statement getAdvanceStatement() {
        return this.advanceStatement;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("Loop {").newLine();
        builder.startBlock();
        builder.appendAttribute("declarations");
        new StatementList(this.variableDeclarations).dump(builder);
        if (this.condition != null) {
            builder.newLine().appendAttribute("condition");
            this.condition.dump(builder);
        }
        if (this.advanceStatement != null) {
            builder.newLine().appendAttribute("advance");
            this.advanceStatement.dump(builder);
        }
        builder.newLine().appendAttribute("body");
        this.body.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LoopStatement that = (LoopStatement) o;

        if (!this.variableDeclarations.equals(that.variableDeclarations))
            return false;
        if (!Objects.equals(this.condition, that.condition))
            return false;
        if (!this.body.equals(that.body))
            return false;
        return Objects.equals(this.advanceStatement, that.advanceStatement);
    }

    @Override
    public int hashCode() {
        int result = this.variableDeclarations.hashCode();
        result = 31 * result + (this.condition != null ? this.condition.hashCode() : 0);
        result = 31 * result + this.body.hashCode();
        result = 31 * result + (this.advanceStatement != null ? this.advanceStatement.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
