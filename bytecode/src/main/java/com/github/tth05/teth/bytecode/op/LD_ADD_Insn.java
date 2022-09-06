package com.github.tth05.teth.bytecode.op;

public class LD_ADD_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_ADD;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
