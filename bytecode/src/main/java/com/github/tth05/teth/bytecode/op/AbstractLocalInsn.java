package com.github.tth05.teth.bytecode.op;

public abstract class AbstractLocalInsn implements IInstrunction {

    private final int localIndex;

    public AbstractLocalInsn(int localIndex) {
        this.localIndex = localIndex;
    }

    public int getLocalIndex() {
        return this.localIndex;
    }

    @Override
    public String getDebugParametersString() {
        return getLocalIndex() + "";
    }
}
