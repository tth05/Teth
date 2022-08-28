package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class LD_GREATER_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_GREATER;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
