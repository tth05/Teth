package com.github.tth05.teth.bytecodeInterpreter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest extends AbstractInterpreterTest {

    @Test
    public void testVariableRedeclarationShouldNotLeakValueToOuterScope() {
        execute("""
                let a = 5
                {
                    let a = 10
                    print([a])
                }
                print([a])
                """);

        assertLinesMatch(List.of("10", "5"), getSystemOutputLines());
    }

    @Test
    public void testLoop() {
        execute("""
                loop (let i = 1, i <= 10, i = i + 1) print([i]) print(["Done"])
                """);

        assertLinesMatch(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Done"), getSystemOutputLines());
    }

    @Test
    public void testSort() {
        execute("""
                let a = [3,2,6,-1,2,10,11]
                sort(a, 7, 0)
                print([a])
                                
                fn sort(a: list<long>, n: long, index: long) {
                    if (index == n) return
                                
                    let k = min(a, index, n-1)
                    if(k != index) {
                        let temp = a.get(k)
                        a.set(k, a.get(index))
                        a.set(index, temp)
                    }
                                
                    sort(a, n, index+1)
                }
                                
                fn min(a: list<long>, i: long, j: long) long {
                    if(i == j) return i
                                
                    let k = min(a, i+1, j)
                                
                    if (a.get(i) < a.get(k))
                        return i
                    else
                        return k
                }
                """);

        assertLinesMatch(List.of("[1, 2, 2, 3, 6, 10, 11]"), getSystemOutputLines());
    }

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
