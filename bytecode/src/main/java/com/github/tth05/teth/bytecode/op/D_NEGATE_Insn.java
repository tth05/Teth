package com.github.tth05.teth.bytecode.op;

public class D_NEGATE_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_NEGATE;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
