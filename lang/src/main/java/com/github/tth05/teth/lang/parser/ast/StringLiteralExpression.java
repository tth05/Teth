package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.util.ASTDumpBuilder;

public class StringLiteralExpression extends Expression {

    private final String value;

    public StringLiteralExpression(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StringLiteralExpression that = (StringLiteralExpression) o;

        return this.value.equals(that.value);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("\"").append(this.value).append("\"");
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
