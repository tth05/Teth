package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class B_AND_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.B_AND;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
