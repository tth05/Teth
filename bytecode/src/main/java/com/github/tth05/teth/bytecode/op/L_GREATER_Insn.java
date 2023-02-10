package com.github.tth05.teth.bytecode.op;

public class L_GREATER_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.L_GREATER;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
