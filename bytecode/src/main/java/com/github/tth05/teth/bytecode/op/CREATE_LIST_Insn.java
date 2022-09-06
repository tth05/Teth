package com.github.tth05.teth.bytecode.op;

public class CREATE_LIST_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.CREATE_LIST;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
