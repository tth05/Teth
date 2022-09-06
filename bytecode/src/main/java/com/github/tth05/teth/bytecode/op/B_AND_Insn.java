package com.github.tth05.teth.bytecode.op;

public class B_AND_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.B_AND;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
