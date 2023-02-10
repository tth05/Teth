package com.github.tth05.teth.bytecode.op;

public class LOAD_MEMBER_Insn extends AbstractMemberInsn {

    public LOAD_MEMBER_Insn(int fieldIndex) {
        super(fieldIndex);
    }

    @Override
    public byte getOpCode() {
        return OpCodes.LOAD_MEMBER;
    }
}
