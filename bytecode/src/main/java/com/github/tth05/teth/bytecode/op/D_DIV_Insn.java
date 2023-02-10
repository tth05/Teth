package com.github.tth05.teth.bytecode.op;

public class D_DIV_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_DIV;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
