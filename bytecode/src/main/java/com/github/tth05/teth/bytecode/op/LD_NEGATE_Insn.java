package com.github.tth05.teth.bytecode.op;

public class LD_NEGATE_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_NEGATE;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
