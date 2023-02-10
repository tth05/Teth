package com.github.tth05.teth.bytecode.op;

public class LOAD_LOCAL_Insn extends AbstractLocalInsn {

    public LOAD_LOCAL_Insn(int localIndex) {
        super(localIndex);
    }

    @Override
    public byte getOpCode() {
        return OpCodes.LOAD_LOCAL;
    }
}
