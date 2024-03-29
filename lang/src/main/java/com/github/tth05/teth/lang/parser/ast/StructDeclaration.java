package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// TODO: Migrate to list with span
public final class StructDeclaration extends Statement implements ITopLevelDeclaration, IHasName {

    private final SourceFileUnit containingUnit;
    private final IdentifierExpression nameExpr;
    private final List<GenericParameterDeclaration> genericParameters;
    private final List<FieldDeclaration> fields;
    private final List<FunctionDeclaration> functions;
    private final boolean intrinsic;

    public StructDeclaration(Span span, SourceFileUnit containingUnit, IdentifierExpression nameExpr, List<GenericParameterDeclaration> genericParameters, List<FieldDeclaration> fields, List<FunctionDeclaration> functions) {
        this(span, containingUnit, nameExpr, genericParameters, fields, functions, false);
    }

    public StructDeclaration(Span span, SourceFileUnit containingUnit, IdentifierExpression nameExpr, List<GenericParameterDeclaration> genericParameters, List<FieldDeclaration> fields, List<FunctionDeclaration> functions, boolean intrinsic) {
        super(span);
        this.containingUnit = containingUnit;
        this.nameExpr = nameExpr;
        this.genericParameters = Collections.unmodifiableList(Objects.requireNonNull(genericParameters));
        this.fields = Collections.unmodifiableList(Objects.requireNonNull(fields));
        this.functions = Collections.unmodifiableList(Objects.requireNonNull(functions));
        this.intrinsic = intrinsic;
    }

    @Override
    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    public List<GenericParameterDeclaration> getGenericParameters() {
        return this.genericParameters;
    }

    public List<FieldDeclaration> getFields() {
        return this.fields;
    }

    public List<FunctionDeclaration> getFunctions() {
        return this.functions;
    }

    @Override
    public SourceFileUnit getContainingUnit() {
        return this.containingUnit;
    }

    public boolean isIntrinsic() {
        return this.intrinsic;
    }

    public Statement getMember(Span name) {
        if (name == null)
            return null;

        return this.fields.stream()
                .filter(f -> Objects.nonNull(f.getNameExpr().getSpan()))
                .filter(f -> f.getNameExpr().getSpan().textEquals(name))
                .map(Statement.class::cast)
                .findFirst()
                .orElse(this.functions.stream()
                        .filter(f -> Objects.nonNull(f.getNameExpr().getSpan()))
                        .filter(f -> f.getNameExpr().getSpan().textEquals(name))
                        .findFirst()
                        .orElse(null));
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("StructDeclaration {").newLine();
        builder.startBlock();
        builder.appendAttribute("name");
        this.nameExpr.dump(builder);
        builder.newLine().appendAttribute("fields", this.fields);
        builder.newLine().appendAttribute("functions", this.functions);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StructDeclaration that = (StructDeclaration) o;

        if (!this.nameExpr.equals(that.nameExpr))
            return false;
        if (!this.fields.equals(that.fields))
            return false;
        return this.functions.equals(that.functions);
    }

    @Override
    public int hashCode() {
        int result = this.nameExpr.hashCode();
        result = 31 * result + this.fields.hashCode();
        result = 31 * result + this.functions.hashCode();
        return result;
    }

    public static final class FieldDeclaration extends Statement implements IVariableDeclaration {

        private final IdentifierExpression nameExpr;
        private final int index;
        private final TypeExpression type;

        public FieldDeclaration(Span span, TypeExpression type, IdentifierExpression nameExpr, int index) {
            super(span);
            this.type = type;
            this.nameExpr = nameExpr;
            this.index = index;
        }

        @Override
        public IdentifierExpression getNameExpr() {
            return this.nameExpr;
        }

        @Override
        public TypeExpression getTypeExpr() {
            return this.type;
        }

        public int getIndex() {
            return this.index;
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

            FieldDeclaration that = (FieldDeclaration) o;

            if (!this.nameExpr.equals(that.nameExpr))
                return false;
            return this.type.equals(that.type);
        }

        @Override
        public int hashCode() {
            int result = this.nameExpr.hashCode();
            result = 31 * result + this.type.hashCode();
            return result;
        }

        @Override
        public void dump(ASTDumpBuilder builder) {
            builder.append("FieldDeclaration {").newLine();
            builder.startBlock();
            builder.appendAttribute("type", this.type.toString()).newLine();
            builder.appendAttribute("name");
            this.nameExpr.dump(builder);
            builder.endBlock().newLine().append("}");
        }

        @Override
        public String toString() {
            return dumpToString();
        }
    }
}
