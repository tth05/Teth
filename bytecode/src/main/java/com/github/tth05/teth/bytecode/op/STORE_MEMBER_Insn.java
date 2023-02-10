package com.github.tth05.teth.bytecode.op;

public class STORE_MEMBER_Insn extends AbstractMemberInsn {

    public STORE_MEMBER_Insn(int fieldIndex) {
        super(fieldIndex);
    }

    @Override
    public byte getOpCode() {
        return OpCodes.STORE_MEMBER;
    }
}
