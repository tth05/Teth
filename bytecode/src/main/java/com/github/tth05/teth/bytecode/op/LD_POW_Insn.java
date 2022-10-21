package com.github.tth05.teth.bytecode.op;

public class LD_POW_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_POW;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
