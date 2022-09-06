package com.github.tth05.teth.bytecode.op;

public class LD_GREATER_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_GREATER;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
