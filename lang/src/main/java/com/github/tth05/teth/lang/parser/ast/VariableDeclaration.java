package com.github.tth05.teth.lang.parser.ast;

import java.util.Objects;

public class VariableDeclaration extends Statement {

    private final String type;
    private final String name;

    private final Expression expression;

    public VariableDeclaration(String type, String name) {
        this(type, name, null);
    }

    public VariableDeclaration(String type, String name, Expression expression) {
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VariableDeclaration that = (VariableDeclaration) o;

        if (!this.type.equals(that.type))
            return false;
        if (!this.name.equals(that.name))
            return false;
        return Objects.equals(this.expression, that.expression);
    }

    @Override
    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + (this.expression != null ? this.expression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VariableDeclaration{" +
               "type='" + this.type + '\'' +
               ", name='" + this.name + '\'' +
               ", expression=" + this.expression +
               '}';
    }
}
