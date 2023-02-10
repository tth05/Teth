package com.github.tth05.teth.bytecode.op;

public class L_ADD_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_ADD;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
