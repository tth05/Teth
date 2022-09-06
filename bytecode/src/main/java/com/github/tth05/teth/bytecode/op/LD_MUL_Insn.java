package com.github.tth05.teth.bytecode.op;

public class LD_MUL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_MUL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
