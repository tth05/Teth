package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.Interpreter;
import com.github.tth05.teth.lang.parser.Type;

public class FunctionValue implements IValue, IFunction {

    private final IFunction function;

    public FunctionValue(IFunction function) {
        this.function = function;
    }

    @Override
    public IValue invoke(Interpreter interpreter, IValue... args) {
        return this.function.invoke(interpreter, args);
    }

    @Override
    public boolean isApplicable(IValue[] parameters) {
        return this.function.isApplicable(parameters);
    }

    @Override
    public String getDebugString() {
        return getName() + "()";
    }

    @Override
    public String getName() {
        return this.function.getName();
    }

    @Override
    public Type getType() {
        return Type.FUNCTION;
    }
}
