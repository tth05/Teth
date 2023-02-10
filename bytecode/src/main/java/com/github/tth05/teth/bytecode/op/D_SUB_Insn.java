package com.github.tth05.teth.bytecode.op;

public class D_SUB_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_SUB;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
