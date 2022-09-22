package com.github.tth05.teth.lang.span;

import java.util.List;

// TODO: Migrate more lists to this type
public interface ListWithSpan<T> extends List<T> {

    default Span getSpan() {
        return getSpanOrElse(null);
    }

    Span getSpanOrElse(Span fallback);

    void setSpan(Span span);
}
