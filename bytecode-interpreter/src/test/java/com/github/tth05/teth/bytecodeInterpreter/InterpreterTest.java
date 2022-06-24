package com.github.tth05.teth.bytecodeInterpreter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest extends AbstractInterpreterTest {

    @Test
    public void testFib() {
        execute("""
                print([fib(30).toString()])
                                
                fn fib(n: long) long {
                    if(n <= 1) return n
                    return fib(n-1) + fib(n-2)
                }
                """);

        assertLinesMatch(List.of("832040"), getSystemOutputLines());
    }
}
