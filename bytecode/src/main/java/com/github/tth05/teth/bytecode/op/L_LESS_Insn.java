package com.github.tth05.teth.bytecode.op;

public class L_LESS_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_LESS;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
