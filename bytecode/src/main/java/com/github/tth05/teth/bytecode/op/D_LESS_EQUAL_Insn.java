package com.github.tth05.teth.bytecode.op;

public class D_LESS_EQUAL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_LESS_EQUAL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
