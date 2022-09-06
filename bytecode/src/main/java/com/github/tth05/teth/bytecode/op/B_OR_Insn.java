package com.github.tth05.teth.bytecode.op;

public class B_OR_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.B_OR;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
