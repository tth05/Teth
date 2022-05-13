package com.github.tth05.teth.lang.parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Expression extends Statement {

    private List<Expression> children;

    public void addChild(Expression expression) {
        initChildren();

        this.children.add(expression);
    }

    private void initChildren() {
        this.children = new ArrayList<>(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Expression that = (Expression) o;

        return Objects.equals(this.children, that.children);
    }

    @Override
    public int hashCode() {
        return this.children != null ? this.children.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Expression{" +
               "children=" + this.children +
               '}';
    }
}
