package com.github.tth05.teth.bytecode.op;

public class D_GREATER_EQUAL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_GREATER_EQUAL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
