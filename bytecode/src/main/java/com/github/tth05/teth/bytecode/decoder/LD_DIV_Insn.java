package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class LD_DIV_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.LD_DIV;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
