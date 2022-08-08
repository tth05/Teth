package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class B_OR_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.B_OR;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
