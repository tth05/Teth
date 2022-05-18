package com.github.tth05.teth.interpreter;

public interface IFunction {

    IValue invoke(IValue... args);
}
