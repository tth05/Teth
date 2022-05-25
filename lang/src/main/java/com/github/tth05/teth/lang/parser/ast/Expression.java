package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.ISpan;

public abstract class Expression extends Statement {

    protected Expression(ISpan span) {
        super(span);
    }
}
