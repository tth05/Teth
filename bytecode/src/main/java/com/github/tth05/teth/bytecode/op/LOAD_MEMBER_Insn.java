package com.github.tth05.teth.bytecode.op;

public class LOAD_MEMBER_Insn implements IInstrunction {

    private final short fieldIndex;

    public LOAD_MEMBER_Insn(short fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public short getFieldIndex() {
        return this.fieldIndex;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.LOAD_MEMBER;
    }

    @Override
    public String getDebugParametersString() {
        return this.fieldIndex + "";
    }
}
