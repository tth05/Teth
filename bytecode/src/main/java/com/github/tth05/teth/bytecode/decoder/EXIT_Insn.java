package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class EXIT_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.EXIT;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
