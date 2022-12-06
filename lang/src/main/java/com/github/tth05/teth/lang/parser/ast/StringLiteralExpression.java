package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class StringLiteralExpression extends Expression implements IDeclarationReference {

    private final List<Part> parts;

    public StringLiteralExpression(List<Part> parts) {
        this(Span.of(parts.get(0).getSpan(), parts.get(parts.size() - 1).getSpan()), parts);
    }

    public StringLiteralExpression(Span span, List<Part> parts) {
        super(span);
        this.parts = Collections.unmodifiableList(Objects.requireNonNull(parts));
    }

    public boolean isSingleString() {
        return this.parts.size() == 1;
    }

    public String asSingleString() {
        if (!isSingleString())
            throw new UnsupportedOperationException("Not a single string");

        return asSingleStringSpan().getText();
    }

    public Span asSingleStringSpan() {
        if (!isSingleString())
            throw new UnsupportedOperationException("Not a single string");

        var span = this.getSpan();
        if (span.length() < 1)
            return span;

        var start = span.offset();
        var end = span.offsetEnd();
        if (span.charAt(0) == '"')
            start++;
        if (span.charAt(span.length() - 1) == '"')
            end--;
        return new Span(span.source(), start, end);
    }

    public List<Part> getParts() {
        return this.parts;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("\"");
        for (var part : this.parts) {
            switch (part.getType()) {
                case STRING -> builder.append(part.getSpan().getText());
                case EXPRESSION -> {
                    builder.append("{");
                    part.asExpression().dump(builder);
                    builder.append("}");
                }
            }
        }
        builder.append("\"");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StringLiteralExpression that = (StringLiteralExpression) o;

        return this.parts.equals(that.parts);
    }

    @Override
    public int hashCode() {
        return this.parts.hashCode();
    }

    @Override
    public String toString() {
        return dumpToString();
    }

    public static Part stringPart(Span span) {
        return new Part(span, PartType.STRING);
    }

    public static Part expressionPart(Expression expression) {
        return new Part(expression.getSpan(), PartType.EXPRESSION, expression);
    }

    public enum PartType {
        STRING,
        EXPRESSION
    }

    public static final class Part {

        private final Span span;
        private final PartType type;
        private final Object value;

        private Part(Span span, PartType type) {
            this(span, type, null);
        }

        private Part(Span span, PartType type, Object value) {
            this.span = span;
            this.type = type;
            this.value = value;
        }

        public Span getSpan() {
            return this.span;
        }

        public PartType getType() {
            return this.type;
        }

        public String asString() {
            if (this.type != PartType.STRING)
                throw new UnsupportedOperationException();
            return this.getSpan().getText();
        }

        public Expression asExpression() {
            if (this.type != PartType.EXPRESSION)
                throw new UnsupportedOperationException();
            return (Expression) this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Part part = (Part) o;

            if (this.type != part.type)
                return false;
            return Objects.equals(this.value, part.value);
        }

        @Override
        public int hashCode() {
            int result = this.type.hashCode();
            result = 31 * result + this.value.hashCode();
            return result;
        }
    }
}
