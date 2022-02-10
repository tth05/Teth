package com.github.tth05.teth.lang.parser.ast;

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
    public String toString() {
        return "VariableDeclaration{" +
               "type='" + this.type + '\'' +
               ", name='" + this.name + '\'' +
               ", expression=" + this.expression +
               '}';
    }
}
