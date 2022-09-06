package com.github.tth05.teth.bytecode.program;

import com.github.tth05.teth.bytecode.op.IInstrunction;

public class TethProgram {

    private final IInstrunction[] instructions;
    private final StructData[] structData;

    public TethProgram(IInstrunction[] instructions, StructData[] structData) {
        this.instructions = instructions;
        this.structData = structData;
    }

    public IInstrunction[] getInstructions() {
        return this.instructions;
    }

    public StructData[] getStructData() {
        return this.structData;
    }
}
