package com.github.tth05.teth.bytecode.op;

public class B_CONST_Insn implements IInstrunction {

    private final boolean value;

    public B_CONST_Insn(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.B_CONST;
    }

    @Override
    public String getDebugParametersString() {
        return this.value + "";
    }
}
