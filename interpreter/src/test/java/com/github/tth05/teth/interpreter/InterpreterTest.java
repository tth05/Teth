package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.parser.ast.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest extends AbstractInterpreterTest {

    private Interpreter interpreter;

    @BeforeEach
    public void init() {
        this.interpreter = new Interpreter();
    }

    @Test
    public void testSimpleMath() {
        createAST("1/2+2*2-3");
        assertStreamsEmpty();

        assertEquals(
                new NumberValue(1.5),
                this.interpreter.evaluateExpression(assertInstanceOf(Expression.class, this.unit.getStatements().get(0)))
        );
    }

    @Test
    public void testBooleanOperators() {
        createAST("5 >= 6 == 2 >= 3");
        assertStreamsEmpty();

        assertEquals(
                new BooleanValue(true),
                this.interpreter.evaluateExpression(assertInstanceOf(Expression.class, this.unit.getStatements().get(0)))
        );
    }
}
