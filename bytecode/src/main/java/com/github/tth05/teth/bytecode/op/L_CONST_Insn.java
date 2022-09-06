package com.github.tth05.teth.bytecode.op;

public class L_CONST_Insn implements IInstrunction {

    private final long value;

    public L_CONST_Insn(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.L_CONST;
    }

    @Override
    public String getDebugParametersString() {
        return this.value + "";
    }
}
