package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.AbstractParserTest;
import com.github.tth05.teth.lang.parser.ast.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest extends AbstractParserTest {

    @Test
    public void testParseString() {
        createAST("double d = \"A string!\"");
        assertEquals(new SourceFileUnit(
                List.of(
                        new VariableDeclaration(
                                "double",
                                "d",
                                new StringLiteralExpression("A string!")
                        )
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseMathExpression() {
        createAST("1+(5 + 5* 2^2)^(100+1)");
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new BinaryExpression(
                                        new LongLiteralExpression(1),

                                        new BinaryExpression(
                                                new BinaryExpression(
                                                        new LongLiteralExpression(5),
                                                        new BinaryExpression(
                                                                new LongLiteralExpression(5),
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
                double d = "a string" + (5 * 5^3) + (5 + 5)*5/5^44
                        
                if (a) {
                    "a is greater than b"
                } else {
                    "b is greater than a"
                }
                        
                if (5) {
                    "a is greater than b"
                } else if (a == b)
                    "b is greater than a"
                        
                if (5 == 5) { 1 } else if (b) c = 2 else
                    c = 5
                """);
        createAST("""
                if (5 == 5) { 1 } else if (b) c = 2 else \nc = 5
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
                                        )
                                ),
                                new ElseStatement(
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
                                                                )
                                                        )
                                                )
                                        )
                                ),
                                new ElseStatement(
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
