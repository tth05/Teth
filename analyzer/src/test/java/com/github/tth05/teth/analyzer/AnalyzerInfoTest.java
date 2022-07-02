package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerInfoTest extends AbstractAnalyzerTest {

    @Test
    public void testFunctionLocalsCount() {
        var problems = analyze("fn f(a: long) {a=5\n let b=3\nlet n = 7}");

        assertTrue(problems.isEmpty());
        assertEquals(2, this.analyzer.functionLocalsCount((FunctionDeclaration) this.unit.getStatements().get(0)));

        problems = analyze("fn f(a: long) {}");

        assertTrue(problems.isEmpty());
        assertEquals(0, this.analyzer.functionLocalsCount((FunctionDeclaration) this.unit.getStatements().get(0)));
    }
}
