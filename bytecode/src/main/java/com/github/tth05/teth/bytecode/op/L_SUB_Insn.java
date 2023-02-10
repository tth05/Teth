package com.github.tth05.teth.bytecode.op;

public class L_SUB_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_SUB;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
