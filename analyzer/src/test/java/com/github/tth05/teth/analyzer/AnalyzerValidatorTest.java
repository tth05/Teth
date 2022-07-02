package com.github.tth05.teth.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerValidatorTest extends AbstractAnalyzerTest {

    @Test
    public void testAssignToVariable() {
        var problems = analyze("let a: long = 10 \n a = 5");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAssignToUndefinedVariable() {
        var problems = analyze("a = 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unresolved identifier", problems.get(0).message());
    }

    @Test
    public void testAssignToVariableInGlobalScope() {
        var problems = analyze("let a:long = 0 {a=5{{a=5}}}a=5");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAssignToVariableOutOfScope() {
        var problems = analyze("{long a}\n a = 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unresolved identifier", problems.get(0).message());
    }

    @Test
    public void testAssignAccessFunctionParameter() {
        var problems = analyze("fn f(a: long) {a=5\n fn b(c: long){c=3}b(a)}");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAssignToParameterOutOfScope() {
        var problems = analyze("fn f(a: long) {} a = 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unresolved identifier", problems.get(0).message());

        problems = analyze("fn f(a: long) {fn b(c: long){a=3}}");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unresolved identifier", problems.get(0).message());
    }

    @Test
    public void testReturnOutsideFunction() {
        var problems = analyze("return 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Return statement outside of function", problems.get(0).message());
    }

    @Test
    public void testInvalidReturnValue() {
        var problems = analyze("fn a() {return 5}");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot return long from function returning void", problems.get(0).message());
    }

    @Test
    public void testAccessMember() {
        var problems = analyze("let f: function = (5).toString");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAccessNonExistentMember() {
        var problems = analyze("let f: function = (5).toStringg");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Member toStringg not found in type long", problems.get(0).message());
    }

    @Test
    public void testIncorrectFunctionInvocation() {
        var problems = analyze("(5+5)()");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Function invocation target must be a function", problems.get(0).message());

        problems = analyze("let a: long = 0\na()");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Function invocation target must be a function", problems.get(0).message());

        problems = analyze("(5).toString(2452, 12358)");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Wrong number of parameters for function invocation. Expected 0, got 2", problems.get(0).message());
    }
}
