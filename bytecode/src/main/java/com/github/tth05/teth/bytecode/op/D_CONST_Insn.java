package com.github.tth05.teth.bytecode.op;

public class D_CONST_Insn implements IInstrunction {

    private final double value;

    public D_CONST_Insn(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.D_CONST;
    }

    @Override
    public String getDebugParametersString() {
        return this.value + "";
    }
}
