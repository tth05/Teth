package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.AbstractParserTest;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;
import com.github.tth05.teth.lang.parser.ast.LongLiteralExpression;
import com.github.tth05.teth.lang.parser.ast.StringLiteralExpression;
import com.github.tth05.teth.lang.parser.ast.VariableDeclaration;
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
