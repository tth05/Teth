package com.github.tth05.teth.bytecode.op;

public class JUMP_Insn implements IJumpInstruction {

    private final int relativeJumpOffset;

    public JUMP_Insn(int relativeJumpOffset) {
        this.relativeJumpOffset = relativeJumpOffset;
    }

    @Override
    public int getRelativeJumpOffset() {
        return this.relativeJumpOffset;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.JUMP;
    }

    @Override
    public String getDebugParametersString() {
        return String.format("relativeJumpOffset: %d", this.relativeJumpOffset);
    }
}
