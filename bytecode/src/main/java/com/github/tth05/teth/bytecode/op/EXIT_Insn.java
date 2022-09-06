package com.github.tth05.teth.bytecode.op;

public class EXIT_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.EXIT;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
