package com.github.tth05.teth.bytecode.op;

public class D_MUL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_MUL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
