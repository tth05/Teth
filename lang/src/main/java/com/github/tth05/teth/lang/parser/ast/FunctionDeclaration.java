package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.IDumpable;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;

public class FunctionDeclaration extends Statement {

    private final String name;
    private final List<Parameter> parameters;
    private final BlockStatement body;

    public FunctionDeclaration(ISpan span, String name, List<Parameter> parameters, BlockStatement body) {
        super(span);
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return this.name;
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
        builder.appendAttribute("name", this.name).newLine();
        builder.appendAttribute("parameters").append("[").newLine().startBlock();
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

        if (!this.name.equals(that.name))
            return false;
        if (!this.parameters.equals(that.parameters))
            return false;
        return this.body.equals(that.body);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
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
