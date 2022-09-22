package com.github.tth05.teth.lang.util;

import java.util.Iterator;
import java.util.List;

public class BiIterator<T> implements Iterator<T> {

    private Iterator<? extends T> first;
    private final Iterator<? extends T> second;

    private BiIterator(Iterator<? extends T> first, Iterator<? extends T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean hasNext() {
        if (this.first == null)
            return this.second.hasNext();

        var r = this.first.hasNext();
        if (!r)
            this.first = null;
        return r || this.second.hasNext();
    }

    @Override
    public T next() {
        return this.first == null ? this.second.next() : this.first.next();
    }

    public static <T> BiIterator<T> of(Iterator<? extends T> first, Iterator<? extends T> second) {
        return new BiIterator<>(first, second);
    }

    public static <T> BiIterator<T> of(List<? extends T> first, List<? extends T> second) {
        return new BiIterator<>(first.iterator(), second.iterator());
    }
}
