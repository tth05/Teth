package com.github.tth05.teth.bytecode.op;

public class LD_DIV_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_DIV;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
