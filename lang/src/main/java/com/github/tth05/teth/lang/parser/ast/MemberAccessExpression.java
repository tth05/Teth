package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class MemberAccessExpression extends Expression implements IDeclarationReference {

    private final IdentifierExpression memberNameExpr;
    private final Expression target;

    public MemberAccessExpression(ISpan span, IdentifierExpression memberNameExpr, Expression target) {
        super(span);
        this.memberNameExpr = memberNameExpr;
        this.target = target;
    }

    public IdentifierExpression getMemberNameExpr() {
        return this.memberNameExpr;
    }

    public Expression getTarget() {
        return this.target;
    }

    @Override
    public IdentifierExpression getReferenceNameExpr() {
        return getMemberNameExpr();
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

        MemberAccessExpression that = (MemberAccessExpression) o;

        if (!this.memberNameExpr.equals(that.memberNameExpr))
            return false;
        return this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = this.memberNameExpr.hashCode();
        result = 31 * result + this.target.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("MemberAccessExpression {").newLine();
        builder.startBlock();
        builder.appendAttribute("memberName");
        this.memberNameExpr.dump(builder);
        builder.newLine().appendAttribute("target");
        this.target.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
