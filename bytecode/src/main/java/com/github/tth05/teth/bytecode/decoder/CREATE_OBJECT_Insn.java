package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class CREATE_OBJECT_Insn implements IInstrunction {

    private final short fieldCount;

    public CREATE_OBJECT_Insn(short fieldCount) {
        this.fieldCount = fieldCount;
    }

    public short getFieldCount() {
        return this.fieldCount;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.CREATE_OBJECT;
    }

    @Override
    public String getDebugParametersString() {
        return this.fieldCount + "";
    }
}
