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
}
