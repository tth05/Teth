package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.IDumpable;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;

public class FunctionDeclaration extends Statement {

    private final IdentifierExpression nameExpr;
    private final List<Parameter> parameters;
    private final BlockStatement body;

    public FunctionDeclaration(ISpan span, IdentifierExpression nameExpr, List<Parameter> parameters, BlockStatement body) {
        super(span);
        this.nameExpr = nameExpr;
        this.parameters = parameters;
        this.body = body;
    }

    public IdentifierExpression getNameExpr() {
        return this.nameExpr;
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public BlockStatement getBody() {
        return this.body;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("FunctionDeclaration {").newLine();
        builder.startBlock();
        builder.appendAttribute("name");
        this.nameExpr.dump(builder);
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
        if (!this.parameters.equals(that.parameters))
            return false;
        return this.body.equals(that.body);
    }

    @Override
    public int hashCode() {
        int result = this.nameExpr.hashCode();
        result = 31 * result + this.parameters.hashCode();
        result = 31 * result + this.body.hashCode();
        return result;
    }

    public record Parameter(Type type, String name) implements IDumpable {

        @Override
        public void dump(ASTDumpBuilder builder) {
            builder.append(this.type.toString()).append(" ").append(this.name);
        }
    }
}
