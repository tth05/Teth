package com.github.tth05.teth.bytecode.op;

public class L_NEGATE_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_NEGATE;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
