package com.github.tth05.teth.bytecode.op;

public class L_DIV_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_DIV;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
