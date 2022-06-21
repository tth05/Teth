package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class DUP_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.DUP;
    }
}
