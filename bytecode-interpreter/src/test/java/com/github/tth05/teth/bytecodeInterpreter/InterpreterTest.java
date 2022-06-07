package com.github.tth05.teth.bytecodeInterpreter;

import org.junit.jupiter.api.Test;

public class InterpreterTest {

    @Test
    public void testFib() {
        var interpreter = new Interpreter();
        interpreter.execute();
    }
}
