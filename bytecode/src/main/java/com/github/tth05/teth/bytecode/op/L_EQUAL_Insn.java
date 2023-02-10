package com.github.tth05.teth.bytecode.op;

public class L_EQUAL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_EQUAL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
