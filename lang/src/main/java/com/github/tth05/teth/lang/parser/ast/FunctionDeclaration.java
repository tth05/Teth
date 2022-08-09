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

public class FunctionDeclaration extends Statement implements IDeclaration {

    private final IdentifierExpression nameExpr;
    private final TypeExpression returnTypeExpr;
    private final List<ParameterDeclaration> parameters;
    private final BlockStatement body;
    private final boolean instanceFunction;
    private final boolean intrinsic;
    private final List<GenericParameterDeclaration> genericParameters;

    public FunctionDeclaration(ISpan span, IdentifierExpression nameExpr,
                               List<GenericParameterDeclaration> genericParameters,
                               List<ParameterDeclaration> parameters, TypeExpression returnTypeExpr,
                               BlockStatement body, boolean instanceFunction) {
        this(span, nameExpr, genericParameters, parameters, returnTypeExpr, body, instanceFunction, false);
    }

    public FunctionDeclaration(ISpan span, IdentifierExpression nameExpr,
                               List<GenericParameterDeclaration> genericParameters,
                               List<ParameterDeclaration> parameters, TypeExpression returnTypeExpr,
                               BlockStatement body, boolean instanceFunction, boolean intrinsic) {
        super(span);
        this.nameExpr = nameExpr;
        this.genericParameters = Collections.unmodifiableList(Objects.requireNonNull(genericParameters));
        this.parameters = Collections.unmodifiableList(Objects.requireNonNull(parameters));
        this.returnTypeExpr = returnTypeExpr;
        this.body = body;
        this.instanceFunction = instanceFunction;
        this.intrinsic = intrinsic;
    }

    @Override
    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    public List<GenericParameterDeclaration> getGenericParameters() {
        return this.genericParameters;
    }

    public List<ParameterDeclaration> getParameters() {
        return this.parameters;
    }

    public TypeExpression getReturnTypeExpr() {
        return this.returnTypeExpr;
    }

    public Type getReturnType() {
        return this.returnTypeExpr == null ? Type.VOID : this.returnTypeExpr.asType();
    }

    public BlockStatement getBody() {
        return this.body;
    }

    public boolean isInstanceFunction() {
        return this.instanceFunction;
    }

    public boolean isIntrinsic() {
        return this.intrinsic;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("FunctionDeclaration {").newLine();
        builder.startBlock().appendAttribute("name");
        this.nameExpr.dump(builder);
        builder.newLine().appendAttribute("returnType");
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
        builder.newLine().appendAttribute("isInstanceMethod", String.valueOf(this.instanceFunction));
        builder.newLine().appendAttribute("isInstrinsic", String.valueOf(this.intrinsic));
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
        if (this.instanceFunction != that.instanceFunction)
            return false;
        if (this.intrinsic != that.intrinsic)
            return false;
        return this.body.equals(that.body);
    }

    @Override
    public int hashCode() {
        int result = this.nameExpr.hashCode();
        result = 31 * result + (this.returnTypeExpr != null ? this.returnTypeExpr.hashCode() : 0);
        result = 31 * result + this.parameters.hashCode();
        result = 31 * result + this.body.hashCode();
        result = 31 * result + (this.instanceFunction ? 1 : 0);
        result = 31 * result + (this.intrinsic ? 1 : 0);
        return result;
    }

    public static final class ParameterDeclaration extends Statement implements IVariableDeclaration, IDumpable {

        private final TypeExpression type;
        private final IdentifierExpression name;

        public ParameterDeclaration(ISpan span, TypeExpression type, IdentifierExpression name) {
            super(span);
            this.type = type;
            this.name = name;
        }

        public ParameterDeclaration(TypeExpression type, IdentifierExpression name) {
            this(Span.of(type.getSpan(), name.getSpan()), type, name);
        }

        @Override
        public void dump(ASTDumpBuilder builder) {
            builder.append(this.type.toString()).append(" ").append(this.name.toString());
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public TypeExpression getTypeExpr() {
            return this.type;
        }

        public IdentifierExpression getNameExpr() {
            return this.name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            var that = (ParameterDeclaration) obj;
            return Objects.equals(this.type, that.type) &&
                   Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.name);
        }
    }
}
