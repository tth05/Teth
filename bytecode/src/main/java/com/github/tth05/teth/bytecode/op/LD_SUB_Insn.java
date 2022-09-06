package com.github.tth05.teth.bytecode.op;

public class LD_SUB_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_SUB;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
