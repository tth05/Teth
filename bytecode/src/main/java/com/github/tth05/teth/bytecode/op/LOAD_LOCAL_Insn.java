package com.github.tth05.teth.bytecode.op;

public class LOAD_LOCAL_Insn implements IInstrunction {

    private final int localIndex;

    public LOAD_LOCAL_Insn(int localIndex) {
        this.localIndex = localIndex;
    }

    public int getLocalIndex() {
        return this.localIndex;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.LOAD_LOCAL;
    }

    @Override
    public String getDebugParametersString() {
        return this.localIndex + "";
    }
}
