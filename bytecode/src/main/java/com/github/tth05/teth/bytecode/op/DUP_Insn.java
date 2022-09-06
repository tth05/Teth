package com.github.tth05.teth.bytecode.op;

public class DUP_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.DUP;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
