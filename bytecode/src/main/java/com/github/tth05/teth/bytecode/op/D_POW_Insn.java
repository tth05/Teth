package com.github.tth05.teth.bytecode.op;

public class D_POW_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_POW;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
