package com.github.tth05.teth.bytecode.op;

public class L_MUL_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_MUL;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
