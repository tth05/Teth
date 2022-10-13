package com.github.tth05.teth.bytecode.op;

public interface IJumpInstruction extends IInstrunction {

    int getRelativeJumpOffset();
}
