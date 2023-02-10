package com.github.tth05.teth.bytecode.op;

public class D_EQUAL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_EQUAL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
