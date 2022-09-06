package com.github.tth05.teth.bytecode.op;

public class B_INVERT_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.B_INVERT;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
