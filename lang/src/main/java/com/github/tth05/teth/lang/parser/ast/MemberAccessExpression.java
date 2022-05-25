package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class MemberAccessExpression extends Expression {

    private final String memberName;
    private final Expression target;

    public MemberAccessExpression(ISpan span, String memberName, Expression target) {
        super(span);
        this.memberName = memberName;
        this.target = target;
    }

    public String getMemberName() {
        return this.memberName;
    }

    public Expression getTarget() {
        return this.target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MemberAccessExpression that = (MemberAccessExpression) o;

        if (!this.memberName.equals(that.memberName))
            return false;
        return this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = this.memberName.hashCode();
        result = 31 * result + this.target.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("MemberAccessExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("memberName", this.memberName).newLine();
        builder.appendAttribute("target");
        this.target.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
