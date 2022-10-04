package com.github.tth05.teth.lang.span;

import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.ArrayList;
import java.util.List;

public class ArrayListWithSpan<T extends Statement> extends ArrayList<T> implements ListWithSpan<T> {

    private Span span;

    public ArrayListWithSpan() {
    }

    public ArrayListWithSpan(int initialCapacity) {
        super(initialCapacity);
    }

    public ArrayListWithSpan(List<T> list) {
        super(list);
    }

    @Override
    public void setSpan(Span span) {
        this.span = span;
    }

    @Override
    public Span getSpanOrElse(Span fallback) {
        if (this.span == null)
            return this.span = Span.of(this, fallback);
        return this.span;
    }

    public static <T extends Statement> ListWithSpan<T> emptyList() {
        return new ArrayListWithSpan<>();
    }
}
