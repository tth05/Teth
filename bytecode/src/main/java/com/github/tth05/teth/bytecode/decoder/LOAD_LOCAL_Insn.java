package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class LOAD_LOCAL_Insn implements IInstrunction {

    private final int localIndex;

    public LOAD_LOCAL_Insn(int localIndex) {
        this.localIndex = localIndex;
    }

    public int getLocalIndex() {
        return this.localIndex;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.LOAD_LOCAL;
    }
}
