package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StructDeclaration extends Statement implements IDeclaration {

    private final IdentifierExpression nameExpr;
    private final List<FieldDeclaration> fields;
    private final List<FunctionDeclaration> functions;

    public StructDeclaration(ISpan span, IdentifierExpression nameExpr, List<FieldDeclaration> fields, List<FunctionDeclaration> functions) {
        super(span);
        this.nameExpr = nameExpr;
        this.fields = Collections.unmodifiableList(Objects.requireNonNull(fields));
        this.functions = Collections.unmodifiableList(Objects.requireNonNull(functions));
    }

    @Override
    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    public List<FieldDeclaration> getFields() {
        return this.fields;
    }

    public List<FunctionDeclaration> getFunctions() {
        return this.functions;
    }

    public Statement getMember(String name) {
        return this.fields.stream()
                .filter(f -> f.getNameExpr().getValue().equals(name))
                .map(Statement.class::cast)
                .findFirst()
                .orElse(this.functions.stream()
                        .filter(f -> f.getNameExpr().getValue().equals(name))
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
        builder.newLine().appendAttribute("fields");
        new StatementList(this.fields).dump(builder);
        builder.newLine().appendAttribute("functions");
        new StatementList(this.functions).dump(builder);
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

    public static class FieldDeclaration extends Statement implements IVariableDeclaration {

        private final IdentifierExpression nameExpr;
        private final int index;
        private final TypeExpression type;

        public FieldDeclaration(ISpan span, TypeExpression type, IdentifierExpression nameExpr, int index) {
            super(span);
            this.type = type;
            this.nameExpr = nameExpr;
            this.index = index;
        }

        public IdentifierExpression getNameExpr() {
            return this.nameExpr;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public TypeExpression getTypeExpr() {
            return this.type;
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
