package com.github.tth05.teth.analyzer.type;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticTypeTest {

    private final TypeCache typeCache = new TypeCache();

    @Test
    public void testIsSubTypeOfAnyAndVoid() {
        var ANY = this.typeCache.getType(Prelude.ANY_STRUCT_DECLARATION);
        var anySubTypes = List.of(
                this.typeCache.getType(Prelude.BOOLEAN_STRUCT_DECLARATION),
                this.typeCache.getType(Prelude.DOUBLE_STRUCT_DECLARATION),
                this.typeCache.getType(Prelude.LONG_STRUCT_DECLARATION),
                ANY,
                this.typeCache.getType(Prelude.STRING_STRUCT_DECLARATION),
                new SemanticType(56),
                new SemanticType(78, List.of(new SemanticType(7999)))
        );
        for (var t : anySubTypes) {
            assertTrue(this.typeCache.isSubtypeOf(t, ANY), () -> "Type " + t + " is not subtype of any");

            if (t != ANY)
                assertFalse(this.typeCache.isSubtypeOf(ANY, t), () -> "Type any is subtype of " + t);
        }

        var VOID = this.typeCache.voidType();
        var notAnySubTypes = List.of(VOID);
        for (var t : notAnySubTypes)
            assertFalse(this.typeCache.isSubtypeOf(t, ANY), () -> "Type " + t + " is subtype of any");

        assertFalse(this.typeCache.isSubtypeOf(VOID, ANY), () -> "Type void is subtype of any");
        assertTrue(this.typeCache.isSubtypeOf(VOID, VOID), () -> "Type void is not subtype of void");
        assertFalse(this.typeCache.isSubtypeOf(ANY, VOID), () -> "Type any is subtype of void");
    }

    @Test
    public void testIsSubTypeOfGenerics() {
        var LONG = this.typeCache.getType(Prelude.LONG_STRUCT_DECLARATION);
        var ANY = this.typeCache.getType(Prelude.ANY_STRUCT_DECLARATION);
        var DOUBLE = this.typeCache.getType(Prelude.DOUBLE_STRUCT_DECLARATION);

        assertFalse(this.typeCache.isSubtypeOf(new SemanticType(5, List.of(LONG)), new SemanticType(5, List.of(DOUBLE))));
        assertTrue(this.typeCache.isSubtypeOf(new SemanticType(5, List.of(LONG)), new SemanticType(5, List.of(LONG))));
        assertTrue(this.typeCache.isSubtypeOf(new SemanticType(5, List.of(LONG)), new SemanticType(5, List.of(ANY))));
    }
}
