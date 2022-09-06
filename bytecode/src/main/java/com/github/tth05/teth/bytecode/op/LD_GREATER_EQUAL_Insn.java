package com.github.tth05.teth.bytecode.op;

public class LD_GREATER_EQUAL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_GREATER_EQUAL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
