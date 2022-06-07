package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class INVOKE_Insn implements IInstrunction {

    private final boolean instanceFunction;
    private final int paramCount;
    private final int localCount;
    private final int absoluteJumpAddress;

    public INVOKE_Insn(boolean instanceFunction, int paramCount, int localCount, int absoluteJumpAddress) {
        this.instanceFunction = instanceFunction;
        this.paramCount = paramCount;
        this.localCount = localCount;
        this.absoluteJumpAddress = absoluteJumpAddress;
    }

    public boolean isInstanceFunction() {
        return this.instanceFunction;
    }

    public int getParamCount() {
        return this.paramCount;
    }

    public int getLocalCount() {
        return this.localCount;
    }

    public int getAbsoluteJumpAddress() {
        return this.absoluteJumpAddress;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.INVOKE;
    }
}
