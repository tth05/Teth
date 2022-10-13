package com.github.tth05.teth.bytecode.op;

public class POP_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.POP;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
