package com.github.tth05.teth.interpreter.environment;

import com.github.tth05.teth.interpreter.values.IValue;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private final Map<String, IValue> localVariables = new HashMap<>();

    public void setLocalVariable(String name, IValue value) {
        this.localVariables.put(name, value);
    }

    public IValue getLocalVariable(String name) {
        return this.localVariables.get(name);
    }
}
