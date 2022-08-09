package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TypeExpression extends Expression implements IDeclarationReference {

    private final String name;
    private final List<TypeExpression> genericBounds;

    public TypeExpression(ISpan span, String name) {
        this(span, name, Collections.emptyList());
    }

    public TypeExpression(ISpan span, String name, List<TypeExpression> genericBounds) {
        super(span);
        this.name = name;
        this.genericBounds = Collections.unmodifiableList(Objects.requireNonNull(genericBounds));
    }

    public String getName() {
        return this.name;
    }

    public List<TypeExpression> getGenericBounds() {
        return this.genericBounds;
    }

    public Type asType() {
        return asType(t -> Type.fromName(t.getName()));
    }

    public Type asType(Function<TypeExpression, Type> basicTypeFactory) {
        if (this.genericBounds.isEmpty()) {
            return basicTypeFactory.apply(this);
        } else {
            return Type.fromNameWithGenericBounds(this.name, this.genericBounds.stream().map(t -> t.asType(basicTypeFactory)).collect(Collectors.toList()));
        }
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
        return this.genericBounds.equals(that.genericBounds);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.genericBounds.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append(this.name + (this.genericBounds.isEmpty() ? "" : "<" + this.genericBounds.stream().map(TypeExpression::toString).collect(Collectors.joining(", ")) + ">"));
    }

    @Override
    public String toString() {
        return dumpToString();
    }
}
