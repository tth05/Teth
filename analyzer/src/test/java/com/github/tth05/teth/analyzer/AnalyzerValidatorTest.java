package com.github.tth05.teth.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerValidatorTest extends AbstractAnalyzerTest {

    @Test
    public void testAssignToVariable() {
        var problems = analyze("long a \n a = 5");

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
        var problems = analyze("long a {a=5{{a=5}}}a=5");

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
        var problems = analyze("fn f(long a) {a=5\n fn b(long c){c=3}b(a)}");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAssignToParameterOutOfScope() {
        var problems = analyze("fn f(long a) {} a = 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unresolved identifier", problems.get(0).message());

        problems = analyze("fn f(long a) {fn b(long c){a=3}}");

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
    public void testAccessMember() {
        var problems = analyze("function f = (5).toString");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAccessNonExistentMember() {
        var problems = analyze("function f = (5).toStringg");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Member toStringg not found in type long", problems.get(0).message());
    }
}
