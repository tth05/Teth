package com.github.tth05.teth.bytecode.op;

public class D_ADD_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_ADD;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
