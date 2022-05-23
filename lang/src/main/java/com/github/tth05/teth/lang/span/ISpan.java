package com.github.tth05.teth.lang.span;

public interface ISpan {

    int getOffset();

    int getLine();

    int getColumn();

    char[] getSource();
}
