package com.github.tth05.teth.bytecode.op;

public class L_TO_D_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_TO_D;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
