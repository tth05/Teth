package com.github.tth05.teth.bytecode.op;

public class CREATE_OBJECT_Insn implements IInstrunction {

    private final int structId;

    public CREATE_OBJECT_Insn(int structId) {
        this.structId = structId;
    }

    public int getStructId() {
        return this.structId;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.CREATE_OBJECT;
    }

    @Override
    public String getDebugParametersString() {
        return this.structId + "";
    }
}
