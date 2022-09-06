package com.github.tth05.teth.bytecode.op;

public class LD_LESS_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_LESS;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
