package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class INVOKE_Insn implements IInstrunction {

    private final boolean instanceFunction;
    private final int paramCount;
    private final int localsCount;
    private final int absoluteJumpAddress;

    public INVOKE_Insn(boolean instanceFunction, int paramCount, int localsCount, int absoluteJumpAddress) {
        this.instanceFunction = instanceFunction;
        this.paramCount = paramCount;
        this.localsCount = localsCount;
        this.absoluteJumpAddress = absoluteJumpAddress;
    }

    public boolean isInstanceFunction() {
        return this.instanceFunction;
    }

    public int getParamCount() {
        return this.paramCount;
    }

    public int getLocalsCount() {
        return this.localsCount;
    }

    public int getAbsoluteJumpAddress() {
        return this.absoluteJumpAddress;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.INVOKE;
    }

    @Override
    public String getDebugParametersString() {
        return String.format("%s, pCount: %d, lCount: %d, target: %d", this.instanceFunction ? "instance" : "static", this.paramCount, this.localsCount, this.absoluteJumpAddress);
    }
}
