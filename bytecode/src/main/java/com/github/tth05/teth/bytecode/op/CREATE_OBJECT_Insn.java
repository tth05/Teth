package com.github.tth05.teth.bytecode.op;

public class CREATE_OBJECT_Insn implements IInstrunction {

    private final int structId;
    private final int fieldCount;

    public CREATE_OBJECT_Insn(int structId, int fieldCount) {
        this.structId = structId;
        this.fieldCount = fieldCount;
    }

    public int getStructId() {
        return this.structId;
    }

    public int getFieldCount() {
        return this.fieldCount;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.CREATE_OBJECT;
    }

    @Override
    public String getDebugParametersString() {
        return String.format("structId: %d, fieldCount: %d", this.structId, this.fieldCount);
    }
}
