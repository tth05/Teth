package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.AbstractParserTest;
import com.github.tth05.teth.lang.parser.ast.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest extends AbstractParserTest {

    @Test
    public void testParseString() {
        createAST("\"A string!\"");
        assertEquals(new SourceFileUnit(
                List.of(
                        new StringLiteralExpression("A string!")
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseBoolean() {
        createAST("true");
        assertEquals(new SourceFileUnit(
                List.of(
                        new BooleanLiteralExpression(true)
                )
        ), this.unit);
        assertStreamsEmpty();

        createAST("false");
        assertEquals(new SourceFileUnit(
                List.of(
                        new BooleanLiteralExpression(false)
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseUnaryExpression() {
        createAST("-(-1 + -2)+!a");
        assertEquals(new SourceFileUnit(
                List.of(
                        new BinaryExpression(
                                new UnaryExpression(
                                        new BinaryExpression(
                                                new UnaryExpression(
                                                        new LongLiteralExpression(1),
                                                        UnaryExpression.Operator.OP_NEGATIVE
                                                ),
                                                new UnaryExpression(
                                                        new LongLiteralExpression(2),
                                                        UnaryExpression.Operator.OP_NEGATIVE
                                                ),
                                                BinaryExpression.Operator.OP_ADD
                                        ),
                                        UnaryExpression.Operator.OP_NEGATIVE
                                ),
                                new UnaryExpression(
                                        new IdentifierExpression("a"),
                                        UnaryExpression.Operator.OP_NOT
                                ),
                                BinaryExpression.Operator.OP_ADD
                        )
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseBinaryExpression() {
        createAST("1+1-1*1/1==1<1<=1>1>=1!=1^(a=1)");
        assertEquals(new SourceFileUnit(
                List.of(
                        new BinaryExpression(
                                new BinaryExpression(
                                        new LongLiteralExpression(1),
                                        new LongLiteralExpression(1),
                                        BinaryExpression.Operator.OP_ADD
                                ),
                                new BinaryExpression(
                                        new BinaryExpression(
                                                new BinaryExpression(
                                                        new LongLiteralExpression(1),
                                                        new LongLiteralExpression(1),
                                                        BinaryExpression.Operator.OP_MULTIPLY
                                                ),
                                                new LongLiteralExpression(1),
                                                BinaryExpression.Operator.OP_DIVIDE
                                        ),
                                        new BinaryExpression(
                                                new BinaryExpression(
                                                        new BinaryExpression(
                                                                new BinaryExpression(
                                                                        new BinaryExpression(
                                                                                new LongLiteralExpression(1),
                                                                                new LongLiteralExpression(1),
                                                                                BinaryExpression.Operator.OP_LESS
                                                                        ),
                                                                        new LongLiteralExpression(1),
                                                                        BinaryExpression.Operator.OP_LESS_EQUAL
                                                                ),
                                                                new LongLiteralExpression(1),
                                                                BinaryExpression.Operator.OP_GREATER
                                                        ),
                                                        new LongLiteralExpression(1),
                                                        BinaryExpression.Operator.OP_GREATER_EQUAL
                                                ),
                                                new BinaryExpression(
                                                        new LongLiteralExpression(1),
                                                        new VariableAssignmentExpression(
                                                                "a",
                                                                new LongLiteralExpression(1)
                                                        ),
                                                        BinaryExpression.Operator.OP_POW
                                                ),
                                                BinaryExpression.Operator.OP_NOT_EQUAL
                                        ),
                                        BinaryExpression.Operator.OP_EQUAL
                                ),
                                BinaryExpression.Operator.OP_SUBTRACT
                        )
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseMathExpression() {
        createAST("1 - 1 + (5 * 6 + 1 + 3 * 2^2)^(100 + 1)");
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new BinaryExpression(
                                        new BinaryExpression(
                                                new LongLiteralExpression(1),
                                                new LongLiteralExpression(1),
                                                BinaryExpression.Operator.OP_SUBTRACT
                                        ),
                                        new BinaryExpression(
                                                new BinaryExpression(
                                                        new BinaryExpression(
                                                                new BinaryExpression(
                                                                        new LongLiteralExpression(5),
                                                                        new LongLiteralExpression(6),
                                                                        BinaryExpression.Operator.OP_MULTIPLY
                                                                ),
                                                                new LongLiteralExpression(1),
                                                                BinaryExpression.Operator.OP_ADD
                                                        ),
                                                        new BinaryExpression(
                                                                new LongLiteralExpression(3),
                                                                new BinaryExpression(
                                                                        new LongLiteralExpression(2),
                                                                        new LongLiteralExpression(2),
                                                                        BinaryExpression.Operator.OP_POW
                                                                ),
                                                                BinaryExpression.Operator.OP_MULTIPLY
                                                        ),
                                                        BinaryExpression.Operator.OP_ADD
                                                ),
                                                new BinaryExpression(
                                                        new LongLiteralExpression(100),
                                                        new LongLiteralExpression(1),
                                                        BinaryExpression.Operator.OP_ADD
                                                ),
                                                BinaryExpression.Operator.OP_POW
                                        ), BinaryExpression.Operator.OP_ADD
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }

    @Test
    public void testParseIfStatement() {
        createAST("""
                if (5 == 5) { 1 } else if (b) c = 2 else \nc = 5
                if (c == 2) "true"
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new IfStatement(
                                        new BinaryExpression(
                                                new LongLiteralExpression(5),
                                                new LongLiteralExpression(5),
                                                BinaryExpression.Operator.OP_EQUAL
                                        ),
                                        new BlockStatement(
                                                StatementList.of(
                                                        new LongLiteralExpression(1)
                                                )
                                        ),
                                        new BlockStatement(
                                                StatementList.of(
                                                        new IfStatement(
                                                                new IdentifierExpression("b"),
                                                                new BlockStatement(
                                                                        StatementList.of(
                                                                                new VariableAssignmentExpression(
                                                                                        "c",
                                                                                        new LongLiteralExpression(2)
                                                                                )
                                                                        )
                                                                ),
                                                                new BlockStatement(
                                                                        StatementList.of(
                                                                                new VariableAssignmentExpression(
                                                                                        "c",
                                                                                        new LongLiteralExpression(5)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                ),
                                new IfStatement(
                                        new BinaryExpression(
                                                new IdentifierExpression("c"),
                                                new LongLiteralExpression(2),
                                                BinaryExpression.Operator.OP_EQUAL
                                        ),
                                        new BlockStatement(
                                                StatementList.of(
                                                        new StringLiteralExpression("true")
                                                )
                                        ),
                                        null
                                )
                        )
                ),
                this.unit
        );
    }

    @Test
    public void testParseVariableDeclaration() {
        createAST("double d = 25");
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new VariableDeclaration(
                                        Type.fromString("double"),
                                        "d",
                                        new LongLiteralExpression(25)
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }

    @Test
    public void testParseFunctionDeclaration() {
        createAST("""
                fn foo(type a, long b) {
                    fn bar() {
                        return "hello"
                    }
                    a
                }
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new FunctionDeclaration(
                                        "foo",
                                        List.of(
                                                new FunctionDeclaration.Parameter(Type.fromString("type"), "a"),
                                                new FunctionDeclaration.Parameter(Type.fromString("long"), "b")
                                        ),
                                        new BlockStatement(
                                                StatementList.of(
                                                        new FunctionDeclaration(
                                                                "bar",
                                                                List.of(),
                                                                new BlockStatement(
                                                                        StatementList.of(
                                                                                new ReturnStatement(new StringLiteralExpression("hello"))
                                                                        )
                                                                )
                                                        ),
                                                        new IdentifierExpression("a")
                                                )
                                        )
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }

    @Test
    public void testParseFunctionInvocation() {
        createAST("""
                1 + (foo(1, 2))("hello world")
                bar()()
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new BinaryExpression(
                                        new LongLiteralExpression(1),
                                        new FunctionInvocationExpression(
                                                new FunctionInvocationExpression(
                                                        new IdentifierExpression("foo"),
                                                        ExpressionList.of(
                                                                new LongLiteralExpression(1),
                                                                new LongLiteralExpression(2)
                                                        )
                                                ),
                                                ExpressionList.of(
                                                        new StringLiteralExpression("hello world")
                                                )
                                        ),
                                        BinaryExpression.Operator.OP_ADD
                                ),
                                new FunctionInvocationExpression(
                                        new FunctionInvocationExpression(
                                                new IdentifierExpression("bar"),
                                                ExpressionList.of()
                                        ),
                                        ExpressionList.of()
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }

    @Test
    public void testParseMemberAccess() {
        createAST("""
                a.b().c().d
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new MemberAccessExpression(
                                        "d",
                                        new FunctionInvocationExpression(
                                                new MemberAccessExpression(
                                                        "c",
                                                        new FunctionInvocationExpression(
                                                                new MemberAccessExpression(
                                                                        "b",
                                                                        new IdentifierExpression("a")
                                                                ),
                                                                ExpressionList.of()
                                                        )
                                                ),
                                                ExpressionList.of()
                                        )
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }
}
