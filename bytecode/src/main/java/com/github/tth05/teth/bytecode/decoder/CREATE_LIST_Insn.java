package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class CREATE_LIST_Insn implements IInstrunction {

    @Override
    public byte getOpCode() {
        return OpCodes.CREATE_LIST;
    }

    @Override
    public String getDebugParametersString() {
        return "";
    }
}
