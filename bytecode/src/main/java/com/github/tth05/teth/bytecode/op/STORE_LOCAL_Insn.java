package com.github.tth05.teth.bytecode.op;

public class STORE_LOCAL_Insn extends AbstractLocalInsn {

    public STORE_LOCAL_Insn(int localIndex) {
        super(localIndex);
    }

    @Override
    public byte getOpCode() {
        return OpCodes.STORE_LOCAL;
    }
}
