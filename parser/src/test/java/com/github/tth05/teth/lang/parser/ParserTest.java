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
    public void testParseUnaryExpression() {
        createAST("-(-1 + -2)");
        assertEquals(new SourceFileUnit(
                List.of(
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
                        )
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseMathExpression() {
        createAST("1-1+(5 + 6* 2^2)^(100+1)");
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new BinaryExpression(
                                        new LongLiteralExpression(1),
                                        new BinaryExpression(
                                                new LongLiteralExpression(1),
                                                new BinaryExpression(
                                                        new BinaryExpression(
                                                                new LongLiteralExpression(5),
                                                                new BinaryExpression(
                                                                        new LongLiteralExpression(6),
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
                                        ),
                                        BinaryExpression.Operator.OP_SUBTRACT
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
                                                                                new AssignmentStatement(
                                                                                        new IdentifierExpression("c"),
                                                                                        new LongLiteralExpression(2)
                                                                                )
                                                                        )
                                                                ),
                                                                new BlockStatement(
                                                                        StatementList.of(
                                                                                new AssignmentStatement(
                                                                                        new IdentifierExpression("c"),
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
                                        "double",
                                        "d",
                                        new LongLiteralExpression(25)
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }
}
