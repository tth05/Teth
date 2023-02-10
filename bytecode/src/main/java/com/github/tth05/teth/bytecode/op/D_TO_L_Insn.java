package com.github.tth05.teth.bytecode.op;

public class D_TO_L_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_TO_L;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
