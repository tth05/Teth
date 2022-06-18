package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.IDumpable;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FunctionDeclaration extends Statement {

    private final IdentifierExpression nameExpr;
    private final TypeExpression returnTypeExpr;
    private final List<ParameterDeclaration> parameters;
    private final BlockStatement body;

    public FunctionDeclaration(ISpan span, IdentifierExpression nameExpr, TypeExpression returnTypeExpr, List<ParameterDeclaration> parameters, BlockStatement body) {
        super(span);
        this.nameExpr = nameExpr;
        this.returnTypeExpr = returnTypeExpr;
        this.parameters = parameters;
        this.body = body;
    }

    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    public TypeExpression getReturnTypeExpr() {
        return this.returnTypeExpr;
    }

    public Type getReturnType() {
        return this.returnTypeExpr == null ? Type.VOID : this.returnTypeExpr.getType();
    }

    public List<ParameterDeclaration> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public BlockStatement getBody() {
        return this.body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("FunctionDeclaration {").newLine();
        builder.startBlock();
        builder.appendAttribute("name");
        this.nameExpr.dump(builder);
        builder.appendAttribute("returnType");
        if (this.returnTypeExpr != null)
            this.returnTypeExpr.dump(builder);
        else
            builder.append("<none>");
        builder.newLine().appendAttribute("parameters").append("[").newLine().startBlock();
        this.parameters.forEach(p -> {
            p.dump(builder);
            builder.newLine();
        });
        builder.endBlock().append("]").newLine().appendAttribute("body");
        this.body.dump(builder);
        builder.endBlock().newLine().append("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FunctionDeclaration that = (FunctionDeclaration) o;

        if (!this.nameExpr.equals(that.nameExpr))
            return false;
        if (!Objects.equals(this.returnTypeExpr, that.returnTypeExpr))
            return false;
        if (!this.parameters.equals(that.parameters))
            return false;
        return this.body.equals(that.body);
    }

    @Override
    public int hashCode() {
        int result = this.nameExpr.hashCode();
        result = 31 * result + (this.returnTypeExpr != null ? this.returnTypeExpr.hashCode() : 0);
        result = 31 * result + this.parameters.hashCode();
        result = 31 * result + this.body.hashCode();
        return result;
    }

    public static final class ParameterDeclaration extends Statement implements IVariableDeclaration, IDumpable {

        private final TypeExpression type;
        private final IdentifierExpression name;
        private final int index;

        public ParameterDeclaration(TypeExpression type, IdentifierExpression name, int index) {
            super(Span.of(type.getSpan(), name.getSpan()));
            this.type = type;
            this.name = name;
            this.index = index;
        }

        @Override
        public void dump(ASTDumpBuilder builder) {
            builder.append(this.type.toString()).append(" ").append(this.name.toString());
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }

        public TypeExpression getTypeExpr() {
            return this.type;
        }

        public IdentifierExpression getNameExpr() {
            return this.name;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            var that = (ParameterDeclaration) obj;
            return Objects.equals(this.type, that.type) &&
                   Objects.equals(this.name, that.name) &&
                   this.index == that.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.name, this.index);
        }
    }
}
