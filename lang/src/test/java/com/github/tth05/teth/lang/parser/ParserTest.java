package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.AbstractParserTest;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;
import com.github.tth05.teth.lang.parser.ast.LongLiteralExpression;
import com.github.tth05.teth.lang.parser.ast.VariableDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest extends AbstractParserTest {

    @Test
    public void parseVariableDeclaration() {
        createAST("double d = (5 + 5* 2^2)^(100+1)");
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new VariableDeclaration(
                                        "double",
                                        "d",
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
                                        )
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }
}
