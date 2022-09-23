package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Expression;
import com.github.tth05.teth.lang.span.ArrayListWithSpan;

import java.util.Collections;

public class ExpressionList extends ArrayListWithSpan<Expression> {

    public static ExpressionList of(Expression... statements) {
        var list = new ExpressionList();
        Collections.addAll(list, statements);
        return list;
    }
}
