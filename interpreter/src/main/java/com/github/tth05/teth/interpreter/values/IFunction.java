package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.Interpreter;

public interface IFunction {

    boolean isApplicable(IValue[] parameters);

    IValue invoke(Interpreter interpreter, IValue... args);

    String getName();
}
