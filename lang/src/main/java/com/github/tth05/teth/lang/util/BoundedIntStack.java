package com.github.tth05.teth.lang.util;

import java.util.NoSuchElementException;

public class BoundedIntStack {

    private final int[] buffer;
    private int idx = -1;

    public BoundedIntStack(int capacity) {
        this.buffer = new int[capacity];
    }

    public int pop() {
        if (isEmpty())
            throw new NoSuchElementException();

        return this.buffer[this.idx--];
    }

    public void push(int value) {
        if (isFull())
            throw new IllegalStateException();

        this.buffer[++this.idx] = value;
    }

    public boolean isEmpty() {
        return this.idx == -1;
    }

    private boolean isFull() {
        return this.idx == this.buffer.length - 1;
    }
}
