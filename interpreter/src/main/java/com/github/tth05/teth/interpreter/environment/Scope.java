package com.github.tth05.teth.interpreter.environment;

import com.github.tth05.teth.interpreter.values.IValue;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private final Map<String, IValue> localVariables = new HashMap<>();

    private final boolean subScope;

    public Scope() {
        this(false);
    }

    public Scope(boolean subScope) {
        this.subScope = subScope;
    }

    public void setLocalVariable(String name, IValue value) {
        this.localVariables.put(name, value);
    }

    public IValue getLocalVariable(String name) {
        return this.localVariables.get(name);
    }

    public boolean hasLocalVariable(String name) {
        return this.localVariables.containsKey(name);
    }

    public boolean isSubScope() {
        return this.subScope;
    }
}
