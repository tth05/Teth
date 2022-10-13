package com.github.tth05.teth.bytecode.op;

public class JUMP_IF_FALSE_Insn implements IJumpInstruction {

    private final int relativeJumpOffset;

    public JUMP_IF_FALSE_Insn(int relativeJumpOffset) {
        this.relativeJumpOffset = relativeJumpOffset;
    }

    @Override
    public int getRelativeJumpOffset() {
        return this.relativeJumpOffset;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.JUMP_IF_FALSE;
    }

    @Override
    public String getDebugParametersString() {
        return String.format("relativeOffset: %d", this.relativeJumpOffset);
    }
}
