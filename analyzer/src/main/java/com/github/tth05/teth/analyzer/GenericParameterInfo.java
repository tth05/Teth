package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.Type;

import java.util.HashMap;
import java.util.Map;

public class GenericParameterInfo {

    private Map<String, Type> boundGenericParameters;

    public boolean isGenericParameterBound(String name) {
        return this.boundGenericParameters != null && this.boundGenericParameters.containsKey(name);
    }

    public void bindGenericParameter(String name, Type type) {
        if (this.boundGenericParameters == null)
            this.boundGenericParameters = new HashMap<>(3);

        this.boundGenericParameters.put(name, type);
    }

    public Type getBoundGenericParameter(String name) {
        if (this.boundGenericParameters == null)
            return null;

        return this.boundGenericParameters.get(name);
    }
}
