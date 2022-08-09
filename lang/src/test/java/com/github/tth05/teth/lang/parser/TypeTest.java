package com.github.tth05.teth.lang.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TypeTest {

    @Test
    public void testIsSubTypeOfAnyAndVoid() {
        var anySubTypes = List.of(Type.BOOLEAN, Type.DOUBLE, Type.LONG, Type.FUNCTION, Type.ANY,
                Type.STRING, new Type("Struct"), new Type("T", List.of(Type.LONG)));
        for (var t : anySubTypes) {
            assertTrue(t.isSubtypeOf(Type.ANY), () -> "Type " + t + " is not subtype of any");

            if (t != Type.ANY)
                assertFalse(Type.ANY.isSubtypeOf(t), () -> "Type any is subtype of " + t);
        }

        var notAnySubTypes = List.of(Type.VOID);
        for (var t : notAnySubTypes)
            assertFalse(t.isSubtypeOf(Type.ANY), () -> "Type " + t + " is subtype of any");

        assertFalse(Type.VOID.isSubtypeOf(Type.ANY), () -> "Type void is subtype of any");
        assertTrue(Type.VOID.isSubtypeOf(Type.VOID), () -> "Type void is not subtype of void");
        assertFalse(Type.ANY.isSubtypeOf(Type.VOID), () -> "Type any is subtype of void");
    }

    @Test
    public void testIsSubTypeOfGenerics() {
        assertFalse(new Type("T", List.of(Type.LONG)).isSubtypeOf(new Type("T", List.of(Type.DOUBLE))));
        assertTrue(new Type("T", List.of(Type.LONG)).isSubtypeOf(new Type("T", List.of(Type.LONG))));
        assertTrue(new Type("T", List.of(Type.LONG)).isSubtypeOf(new Type("T", List.of(Type.ANY))));
    }
}
