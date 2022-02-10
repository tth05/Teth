package com.github.tth05.teth.lang.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class Expression {

    private List<Expression> children;

    public void addChild(Expression expression) {
        initChildren();

        this.children.add(expression);
    }

    private void initChildren() {
        this.children = new ArrayList<>(1);
    }

    @Override
    public String toString() {
        return "Expression{" +
               "children=" + this.children +
               '}';
    }
}
