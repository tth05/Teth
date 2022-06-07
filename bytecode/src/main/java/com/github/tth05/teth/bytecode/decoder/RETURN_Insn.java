package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

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
}
