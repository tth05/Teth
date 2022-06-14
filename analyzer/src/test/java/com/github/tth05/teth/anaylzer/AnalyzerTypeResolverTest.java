package com.github.tth05.teth.anaylzer;

import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.Expression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerTypeResolverTest extends AbstractAnalyzerTest {

    @Test
    public void testBinaryExpressionLong() {
        var problems = analyze("1+1");

        assertTrue(problems.isEmpty());
        assertEquals(Type.LONG, this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));
    }

    @Test
    public void testBinaryExpressionImplicitDouble() {
        var problems = analyze("1+1.0");

        assertTrue(problems.isEmpty());
        assertEquals(Type.DOUBLE, this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));
    }

    @Test
    public void testBinaryIncompatibleTypes() {
        var problems = analyze("1+true");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Operator + cannot be applied to long and bool", problems.get(0).message());

        problems = analyze("\"5\"-515");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Operator - cannot be applied to string and long", problems.get(0).message());

        problems = analyze("(5+6)-3.0*\"5\"");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Operator * cannot be applied to double and string", problems.get(0).message());
    }

    @Test
    public void testUnaryExpressionNumber() {
        var problems = analyze("-56");

        assertTrue(problems.isEmpty());
        assertEquals(Type.LONG, this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));

        problems = analyze("-56.0");

        assertTrue(problems.isEmpty());
        assertEquals(Type.DOUBLE, this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));
    }

    @Test
    public void testUnaryExpressionBoolean() {
        var problems = analyze("!!(true)");

        assertTrue(problems.isEmpty());
        assertEquals(Type.BOOLEAN, this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));
    }

    @Test
    public void testUnaryIncompatibleTypes() {
        var problems = analyze("-true");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unary operator - cannot be applied to bool", problems.get(0).message());

        problems = analyze("-\"5\"");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unary operator - cannot be applied to string", problems.get(0).message());

        problems = analyze("!5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unary operator ! cannot be applied to long", problems.get(0).message());
    }

    @Test
    public void testListExpression() {
        var problems = analyze("[]");

        assertTrue(problems.isEmpty());
        assertEquals(new Type(Type.ANY), this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));

        problems = analyze("[-56, 5, 1]");

        assertTrue(problems.isEmpty());
        assertEquals(new Type(Type.LONG), this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));

        problems = analyze("[\"5\", \"1\", \"2\"]");

        assertTrue(problems.isEmpty());
        assertEquals(new Type(Type.STRING), this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));

        problems = analyze("[[5]]");

        assertTrue(problems.isEmpty());
        assertEquals(new Type(new Type(Type.LONG)), this.analyzer.resolvedType(((Expression) this.unit.getStatements().get(0))));
    }

    @Test
    public void testListExpressionIncompatibleTypes() {
        var problems = analyze("[5, 5.0]");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("List element type mismatch", problems.get(0).message());

        problems = analyze("[\"\", 5]");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("List element type mismatch", problems.get(0).message());

        problems = analyze("[true, false, 1]");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("List element type mismatch", problems.get(0).message());
    }
}
