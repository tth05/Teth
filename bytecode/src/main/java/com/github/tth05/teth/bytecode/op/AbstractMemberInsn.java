package com.github.tth05.teth.bytecode.op;

public abstract class AbstractMemberInsn implements IInstrunction {

    private final int fieldIndex;

    public AbstractMemberInsn(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public int getFieldIndex() {
        return this.fieldIndex;
    }

    @Override
    public String getDebugParametersString() {
        return getFieldIndex() + "";
    }
}
