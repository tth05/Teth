package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.analyzer.type.SemanticType;

import java.util.HashMap;
import java.util.Map;

public class GenericParameterInfo {

    private Map<String, SemanticType> boundGenericParameters;

    public boolean isGenericParameterBound(String name) {
        return this.boundGenericParameters != null && this.boundGenericParameters.containsKey(name);
    }

    public void bindGenericParameter(String name, SemanticType type) {
        if (this.boundGenericParameters == null)
            this.boundGenericParameters = new HashMap<>(3);

        this.boundGenericParameters.put(name, type);
    }

    public SemanticType getBoundGenericParameter(String name) {
        if (this.boundGenericParameters == null)
            return null;

        return this.boundGenericParameters.get(name);
    }
}
