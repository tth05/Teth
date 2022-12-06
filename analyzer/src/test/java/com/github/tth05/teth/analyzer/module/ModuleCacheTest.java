package com.github.tth05.teth.analyzer.module;

import com.github.tth05.teth.lang.span.Span;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleCacheTest {

    @Test
    public void testInvalidModulePaths() {
        var invalidPaths = List.of(
                "..",
                "test/..",
                "/test",
                "test2//test",
                "test2/./test",
                "test.test"
        );

        for (var path : invalidPaths) {
            assertFalse(ModuleCache.isValidModulePath(Span.fromString(path)));
        }
    }

    @Test
    public void testValidModulePaths() {
        var validModulePaths = List.of(
                "test",
                "test/test",
                "../test/../test/../test"
        );

        for (var path : validModulePaths) {
            assertTrue(ModuleCache.isValidModulePath(Span.fromString(path)));
        }
    }
}
