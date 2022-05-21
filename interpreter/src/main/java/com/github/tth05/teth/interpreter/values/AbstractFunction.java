package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.lang.parser.Type;

public abstract class AbstractFunction implements IFunction {

    private final boolean varargs;
    private final Type[] parameterTypes;
    private final String name;

    public AbstractFunction(String name, boolean varargs, Type... parameterTypes) {
        if (varargs && parameterTypes.length != 1)
            throw new IllegalArgumentException("Varargs functions must have exactly one parameter type");

        this.name = name;
        this.varargs = varargs;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean isApplicable(IValue[] parameters) {
        if (this.varargs) {
            for (IValue parameter : parameters) {
                if (!parameter.getType().equals(this.parameterTypes[0]))
                    return false;
            }

            return true;
        }

        if (parameters.length != this.parameterTypes.length)
            return false;

        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].getType().equals(this.parameterTypes[i]))
                return false;
        }

        return true;
    }

    public String getName() {
        return this.name;
    }
}
