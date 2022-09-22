package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class TypeExpression extends Expression implements IDeclarationReference {

    private final String name;
    private final List<TypeExpression> genericParameters;

    public TypeExpression(Span span, String name) {
        this(span, name, Collections.emptyList());
    }

    public TypeExpression(Span span, String name, List<TypeExpression> genericParameters) {
        super(span);
        this.name = name;
        this.genericParameters = Collections.unmodifiableList(Objects.requireNonNull(genericParameters));
    }

    public String getName() {
        return this.name;
    }

    public List<TypeExpression> getGenericParameters() {
        return this.genericParameters;
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

        TypeExpression that = (TypeExpression) o;

        if (!this.name.equals(that.name))
            return false;
        return this.genericParameters.equals(that.genericParameters);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.genericParameters.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        if (this.name == null)
            builder.append("???");
        else
            builder.append(this.name + (this.genericParameters.isEmpty() ? "" : "<" + this.genericParameters.stream().map(TypeExpression::toString).collect(Collectors.joining(", ")) + ">"));
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
