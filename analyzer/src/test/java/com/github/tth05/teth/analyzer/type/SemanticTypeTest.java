package com.github.tth05.teth.analyzer.type;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticTypeTest {

    private final TypeCache typeCache = new TypeCache();

    @Test
    public void testIsSubTypeOfAnyAndVoid() {
        var ANY = this.typeCache.getType(Prelude.getAnyStruct());
        var anySubTypes = List.of(
                this.typeCache.getType(Prelude.getBoolStruct()),
                this.typeCache.getType(Prelude.getDoubleStruct()),
                this.typeCache.getType(Prelude.getLongStruct()),
                ANY,
                this.typeCache.getType(Prelude.getStringStruct()),
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
    public void testIsSubTypeOfNull() {
        var nullType = SemanticType.NULL;
        var nullSuperTypes = List.of(
                this.typeCache.getType(Prelude.getAnyStruct()),
                this.typeCache.getType(Prelude.getBoolStruct()),
                this.typeCache.getType(Prelude.getDoubleStruct()),
                this.typeCache.getType(Prelude.getLongStruct()),
                this.typeCache.getType(Prelude.getStringStruct()),
                new SemanticType(56),
                new SemanticType(78, List.of(new SemanticType(7999)))
        );

        for (var t : nullSuperTypes) {
            assertTrue(this.typeCache.isSubtypeOf(nullType, t), () -> "Type " + t + " is not supertype of null");
            assertFalse(this.typeCache.isSubtypeOf(t, nullType), () -> "Type " + t + " is subtype of null");
        }
    }

    @Test
    public void testIsSubTypeOfGenerics() {
        var LONG = this.typeCache.getType(Prelude.getLongStruct());
        var ANY = this.typeCache.getType(Prelude.getAnyStruct());
        var DOUBLE = this.typeCache.getType(Prelude.getDoubleStruct());

        assertFalse(this.typeCache.isSubtypeOf(new SemanticType(5, List.of(LONG)), new SemanticType(5, List.of(DOUBLE))));
        assertTrue(this.typeCache.isSubtypeOf(new SemanticType(5, List.of(LONG)), new SemanticType(5, List.of(LONG))));
        assertTrue(this.typeCache.isSubtypeOf(new SemanticType(5, List.of(LONG)), new SemanticType(5, List.of(ANY))));
    }
}
