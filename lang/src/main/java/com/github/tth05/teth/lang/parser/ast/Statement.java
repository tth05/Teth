package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.IDumpable;
import com.github.tth05.teth.lang.parser.IVisitable;
import com.github.tth05.teth.lang.span.ISpan;

public abstract class Statement implements IDumpable, IVisitable {

    private final ISpan span;

    protected Statement(ISpan span) {
        this.span = span;
    }

    public ISpan getSpan() {
        return this.span;
    }
}
