package com.github.tth05.teth.bytecode.compiler.optimization;

import com.github.tth05.teth.bytecode.op.IInstrunction;

import java.util.List;

public interface IOptimizer {

    void method(List<IInstrunction> instructions);
}
