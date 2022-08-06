package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class STORE_MEMBER_Insn implements IInstrunction {

    private final short fieldIndex;

    public STORE_MEMBER_Insn(short fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public short getFieldIndex() {
        return this.fieldIndex;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.STORE_MEMBER;
    }

    @Override
    public String getDebugParametersString() {
        return this.fieldIndex + "";
    }
}
