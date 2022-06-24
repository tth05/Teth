package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class S_CONST_Insn implements IInstrunction {

    private final String value;

    public S_CONST_Insn(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.S_CONST;
    }

    @Override
    public String getDebugParametersString() {
        return this.value;
    }
}
