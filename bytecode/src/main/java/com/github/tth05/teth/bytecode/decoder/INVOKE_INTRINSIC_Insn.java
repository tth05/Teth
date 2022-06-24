package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class INVOKE_INTRINSIC_Insn implements IInstrunction {

    private final String intrinsicName;

    public INVOKE_INTRINSIC_Insn(String intrinsicName) {
        this.intrinsicName = intrinsicName;
    }

    public String getIntrinsicName() {
        return this.intrinsicName;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.INVOKE_INTRINSIC;
    }

    @Override
    public String getDebugParametersString() {
        return this.intrinsicName;
    }
}
