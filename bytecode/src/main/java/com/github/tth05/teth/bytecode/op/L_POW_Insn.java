package com.github.tth05.teth.bytecode.op;

public class L_POW_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_POW;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
