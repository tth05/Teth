package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.Span;

public abstract class Expression extends Statement {

    protected Expression(Span span) {
        super(span);
    }
}
