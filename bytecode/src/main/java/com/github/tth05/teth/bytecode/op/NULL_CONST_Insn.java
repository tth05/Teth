package com.github.tth05.teth.bytecode.op;

public class NULL_CONST_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.NULL_CONST;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
