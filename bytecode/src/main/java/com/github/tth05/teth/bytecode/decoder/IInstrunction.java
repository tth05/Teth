package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

import java.lang.reflect.Field;

public interface IInstrunction {

    byte getOpCode();

    default String getDebugString() {
        var opCode = getOpCode();

        try {
            for (Field field : OpCodes.class.getFields()) {
                if (field.getInt(null) == opCode) {
                    return String.format("(%2d)%-16s %s", opCode, field.getName(), getDebugParametersString());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException();
    }

    String getDebugParametersString();
}
