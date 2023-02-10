package com.github.tth05.teth.bytecode.op;

public class D_GREATER_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.D_GREATER;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
