package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;

public class JUMP_IF_TRUE_Insn implements IInstrunction {

    private final int relativeJumpOffset;

    public JUMP_IF_TRUE_Insn(int relativeJumpOffset) {
        this.relativeJumpOffset = relativeJumpOffset;
    }

    public int getRelativeJumpOffset() {
        return this.relativeJumpOffset;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.JUMP_IF_TRUE;
    }

    @Override
    public String getDebugParametersString() {
        return String.format("relativeOffset: %d", this.relativeJumpOffset);
    }
}
