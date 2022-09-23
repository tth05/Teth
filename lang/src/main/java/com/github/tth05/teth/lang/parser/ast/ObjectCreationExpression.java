package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.ExpressionList;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObjectCreationExpression extends Expression implements IDeclarationReference {

    private final IdentifierExpression targetNameExpr;
    private final List<TypeExpression> genericParameters;
    private final ExpressionList parameters;

    public ObjectCreationExpression(Span span, IdentifierExpression targetNameExpr, List<TypeExpression> genericParameters, ExpressionList parameters) {
        super(span);
        this.targetNameExpr = targetNameExpr;
        this.genericParameters = Collections.unmodifiableList(Objects.requireNonNull(genericParameters));
        this.parameters = parameters;
    }

    public IdentifierExpression getTargetNameExpr() {
        return this.targetNameExpr;
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
        builder.append("ObjectCreationExpression {").startBlock().newLine();
        builder.appendAttribute("targetName");
        this.targetNameExpr.dump(builder);
        builder.newLine().appendAttribute("parameters", this.parameters);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ObjectCreationExpression that = (ObjectCreationExpression) o;

        if (!this.targetNameExpr.equals(that.targetNameExpr))
            return false;
        return this.parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        int result = this.targetNameExpr.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }
}
