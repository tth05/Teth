package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.interpreter.values.BooleanValue;
import com.github.tth05.teth.interpreter.values.DoubleValue;
import com.github.tth05.teth.interpreter.values.LongValue;
import com.github.tth05.teth.lang.parser.ast.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
                new LongValue(1),
                this.interpreter.evaluateExpression(assertInstanceOf(Expression.class, this.unit.getStatements().get(0)))
        );

        createAST("1.1/2.5+2.25*2.25-3.123");
        assertStreamsEmpty();

        var result = this.interpreter.evaluateExpression(assertInstanceOf(Expression.class, this.unit.getStatements().get(0)));
        var val = assertInstanceOf(DoubleValue.class, result);
        assertEquals(2.3795, val.getValue(), 0.000001);
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

    @Test
    public void testUnaryOperators() {
        createAST("-5 == 2 == !true");
        assertStreamsEmpty();

        assertEquals(
                new BooleanValue(true),
                this.interpreter.evaluateExpression(assertInstanceOf(Expression.class, this.unit.getStatements().get(0)))
        );
    }

    @Test
    public void testFunctionCall() {
        createAST("""
                fn foo(long x)
                {
                        fn foo2(long x, bool b) {
                                if(b) print(x+1) else print(x-1)
                        }
                        foo2(x, true)
                        foo2(x, false)
                }
                                
                foo(5)
                """);
        assertStreamsEmpty();

        this.interpreter.execute(this.unit);
        assertLinesMatch(List.of("6", "4"), getSystemOutputLines());
    }

    @Test
    public void testVariableAssignment() {
        createAST("""
                long x = 5 + 2
                print(x)
                long y = x + 1
                x = 1
                y = 1 + -y
                double z = 0.555
                z = z+0.1
                print(x, y, z)
                """);
        assertStreamsEmpty();

        this.interpreter.execute(this.unit);
        assertLinesMatch(List.of("7", "1 -7 0.655"), getSystemOutputLines());
    }
    @Test
    public void testMemberAccessAndFunctionVariables() {
        createAST("""
                function x = print
                long y = 5
                function b = y.toBinaryString
                x(b(y) + " " + y.toBinaryString().length().toString())
                """);
        assertStreamsEmpty();

        this.interpreter.execute(this.unit);
        assertLinesMatch(List.of("101 3"), getSystemOutputLines());
    }
}
