package com.github.tth05.teth.bytecode.op;

public class RETURN_Insn implements IInstrunction {

    private final boolean returnsValue;

    public RETURN_Insn(boolean returnsValue) {
        this.returnsValue = returnsValue;
    }

    public boolean shouldReturnValue() {
        return this.returnsValue;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.RETURN;
    }

    @Override
    public String getDebugParametersString() {
        return this.returnsValue ? "value" : "";
    }
}
