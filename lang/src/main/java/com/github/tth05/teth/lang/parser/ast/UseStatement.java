package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class UseStatement extends Statement implements ITopLevelDeclaration {

    private final IdentifierExpression path;
    private final List<IdentifierExpression> imports;

    public UseStatement(Span span, IdentifierExpression path, List<IdentifierExpression> imports) {
        super(span);
        this.path = path;
        this.imports = Collections.unmodifiableList(Objects.requireNonNull(imports));
    }

    public IdentifierExpression getPathExpr() {
        return this.path;
    }

    public List<IdentifierExpression> getImports() {
        return this.imports;
    }

    @Override
    public SourceFileUnit getContainingUnit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UseStatement that = (UseStatement) o;

        if (!this.path.equals(that.path))
            return false;
        return this.imports.equals(that.imports);
    }

    @Override
    public int hashCode() {
        int result = this.path.hashCode();
        result = 31 * result + this.imports.hashCode();
        return result;
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("UseStatement {").newLine().startBlock();
        builder.appendAttribute("path", this.path.getValue()).newLine();
        builder.appendAttribute("imports", this.imports.stream().map(IdentifierExpression::getValue).collect(Collectors.joining(", "))).newLine();
        builder.endBlock().append("}");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
