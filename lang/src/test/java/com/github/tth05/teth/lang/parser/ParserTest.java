package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.AbstractParserTest;
import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.source.InMemorySource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest extends AbstractParserTest {

    @Test
    public void testParseString() {
        createAST("\"A string!\"");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "A string!")))
        )
        ), this.unit);

        createAST("""
                "5+{5} is equal to \\{=}{calc(5+5)}"
                """);
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new StringLiteralExpression(null, List.of(
                        StringLiteralExpression.stringPart(null, "5+"),
                        StringLiteralExpression.expressionPart(new LongLiteralExpression(null, 5)),
                        StringLiteralExpression.stringPart(null, " is equal to {=}"),
                        StringLiteralExpression.expressionPart(new FunctionInvocationExpression(
                                        null,
                                        new IdentifierExpression(null, "calc"),
                                        List.of(),
                                        ExpressionList.of(
                                                new BinaryExpression(
                                                        null,
                                                        new LongLiteralExpression(null, 5),
                                                        new LongLiteralExpression(null, 5),
                                                        BinaryExpression.Operator.OP_ADD
                                                )
                                        )
                                )
                        ),
                        StringLiteralExpression.stringPart(null, "")
                ))
        )
        ), this.unit);

        createAST("""
                "a""b""c"
                """);
        assertEquals(
                new SourceFileUnit(
                        "main",
                        StatementList.of(
                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "a"))),
                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "b"))),
                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "c")))
                        )
                ),
                this.unit
        );
    }

    @Test
    public void testParseBoolean() {
        createAST("true");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new BooleanLiteralExpression(null, true)
        )
        ), this.unit);

        createAST("false");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new BooleanLiteralExpression(null, false)
        )
        ), this.unit);
    }

    @Test
    public void testParseListLiteral() {
        createAST("[5,6,7]");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new ListLiteralExpression(
                        null,
                        ExpressionList.of(
                                new LongLiteralExpression(null, 5),
                                new LongLiteralExpression(null, 6),
                                new LongLiteralExpression(null, 7)
                        )
                )
        )
        ), this.unit);
    }

    @Test
    public void testParseTypes() {
        createAST("""
                let l: long = 5
                let l: double = 5
                let l: string = 5
                let l: boolean = 5
                let l: any = 5
                let l: function = 5
                let l: list<list<long, double<long>>> = 5
                """);
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new VariableDeclaration(
                        null,
                        new TypeExpression(null, new IdentifierExpression(null, "long")),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                ),
                new VariableDeclaration(
                        null,
                        new TypeExpression(null, new IdentifierExpression(null, "double")),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                ),
                new VariableDeclaration(
                        null,
                        new TypeExpression(null, new IdentifierExpression(null, "string")),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                ),
                new VariableDeclaration(
                        null,
                        new TypeExpression(null, new IdentifierExpression(null, "boolean")),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                ),
                new VariableDeclaration(
                        null,
                        new TypeExpression(null, new IdentifierExpression(null, "any")),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                ),
                new VariableDeclaration(
                        null,
                        new TypeExpression(null, new IdentifierExpression(null, "function")),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                ),
                new VariableDeclaration(
                        null,
                        new TypeExpression(null,
                                new IdentifierExpression(null, "list"),
                                List.of(
                                        new TypeExpression(null,
                                                new IdentifierExpression(null, "list"),
                                                List.of(
                                                        new TypeExpression(null, new IdentifierExpression(null, "long")),
                                                        new TypeExpression(null, new IdentifierExpression(null, "double"), List.of(new TypeExpression(null, new IdentifierExpression(null, "long"))))
                                                )
                                        )
                                )
                        ),
                        new IdentifierExpression(null, "l"),
                        new LongLiteralExpression(null, 5)
                )
        )
        ), this.unit);
    }

    @Test
    public void testParseUnaryExpression() {
        createAST("-(-1 + -2)+!a");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new BinaryExpression(
                        null,
                        new UnaryExpression(
                                null,
                                new ParenthesisedExpression(null,
                                        new BinaryExpression(
                                                null,
                                                new UnaryExpression(
                                                        null,
                                                        new LongLiteralExpression(null, 1),
                                                        UnaryExpression.Operator.OP_NEGATIVE
                                                ),
                                                new UnaryExpression(
                                                        null,
                                                        new LongLiteralExpression(null, 2),
                                                        UnaryExpression.Operator.OP_NEGATIVE
                                                ),
                                                BinaryExpression.Operator.OP_ADD
                                        )
                                ),
                                UnaryExpression.Operator.OP_NEGATIVE
                        ),
                        new UnaryExpression(
                                null,
                                new IdentifierExpression(null, "a"),
                                UnaryExpression.Operator.OP_NOT
                        ),
                        BinaryExpression.Operator.OP_ADD
                )
        )
        ), this.unit);
    }

    @Test
    public void testParseBinaryExpression() {
        createAST("1+1-1*1/1==1<1<=1>1>=1!=1^(a=1)");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new BinaryExpression(
                        null,
                        new BinaryExpression(
                                null,
                                new LongLiteralExpression(null, 1),
                                new LongLiteralExpression(null, 1),
                                BinaryExpression.Operator.OP_ADD
                        ),
                        new BinaryExpression(
                                null,
                                new BinaryExpression(
                                        null,
                                        new BinaryExpression(
                                                null,
                                                new LongLiteralExpression(null, 1),
                                                new LongLiteralExpression(null, 1),
                                                BinaryExpression.Operator.OP_MULTIPLY
                                        ),
                                        new LongLiteralExpression(null, 1),
                                        BinaryExpression.Operator.OP_DIVIDE
                                ),
                                new BinaryExpression(
                                        null,
                                        new BinaryExpression(
                                                null,
                                                new BinaryExpression(
                                                        null,
                                                        new BinaryExpression(
                                                                null,
                                                                new BinaryExpression(
                                                                        null,
                                                                        new LongLiteralExpression(null, 1),
                                                                        new LongLiteralExpression(null, 1),
                                                                        BinaryExpression.Operator.OP_LESS
                                                                ),
                                                                new LongLiteralExpression(null, 1),
                                                                BinaryExpression.Operator.OP_LESS_EQUAL
                                                        ),
                                                        new LongLiteralExpression(null, 1),
                                                        BinaryExpression.Operator.OP_GREATER
                                                ),
                                                new LongLiteralExpression(null, 1),
                                                BinaryExpression.Operator.OP_GREATER_EQUAL
                                        ),
                                        new BinaryExpression(
                                                null,
                                                new LongLiteralExpression(null, 1),
                                                new ParenthesisedExpression(
                                                        null,
                                                        new BinaryExpression(
                                                                null,
                                                                new IdentifierExpression(null, "a"),
                                                                new LongLiteralExpression(null, 1),
                                                                BinaryExpression.Operator.OP_ASSIGN
                                                        )
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

        createAST("a && b || c && d");
        assertEquals(new SourceFileUnit(
                "main", StatementList.of(
                new BinaryExpression(
                        null,
                        new BinaryExpression(
                                null,
                                new IdentifierExpression(null, "a"),
                                new IdentifierExpression(null, "b"),
                                BinaryExpression.Operator.OP_AND
                        ),
                        new BinaryExpression(
                                null,
                                new IdentifierExpression(null, "c"),
                                new IdentifierExpression(null, "d"),
                                BinaryExpression.Operator.OP_AND
                        ),
                        BinaryExpression.Operator.OP_OR
                )
        )
        ), this.unit);
    }

    @Test
    public void testParseMathExpression() {
        createAST("1 - 1 + (5 * 6 + 1 + 3 * 2^2)^(100 + 1)");
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new BinaryExpression(
                                null,
                                new BinaryExpression(
                                        null,
                                        new LongLiteralExpression(null, 1),
                                        new LongLiteralExpression(null, 1),
                                        BinaryExpression.Operator.OP_SUBTRACT
                                ),
                                new BinaryExpression(
                                        null,
                                        new ParenthesisedExpression(
                                                null,
                                                new BinaryExpression(
                                                        null,
                                                        new BinaryExpression(
                                                                null,
                                                                new BinaryExpression(
                                                                        null,
                                                                        new LongLiteralExpression(null, 5),
                                                                        new LongLiteralExpression(null, 6),
                                                                        BinaryExpression.Operator.OP_MULTIPLY
                                                                ),
                                                                new LongLiteralExpression(null, 1),
                                                                BinaryExpression.Operator.OP_ADD
                                                        ),
                                                        new BinaryExpression(
                                                                null,
                                                                new LongLiteralExpression(null, 3),
                                                                new BinaryExpression(
                                                                        null,
                                                                        new LongLiteralExpression(null, 2),
                                                                        new LongLiteralExpression(null, 2),
                                                                        BinaryExpression.Operator.OP_POW
                                                                ),
                                                                BinaryExpression.Operator.OP_MULTIPLY
                                                        ),
                                                        BinaryExpression.Operator.OP_ADD
                                                )
                                        ),
                                        new ParenthesisedExpression(
                                                null,
                                                new BinaryExpression(
                                                        null,
                                                        new LongLiteralExpression(null, 100),
                                                        new LongLiteralExpression(null, 1),
                                                        BinaryExpression.Operator.OP_ADD
                                                )
                                        ),
                                        BinaryExpression.Operator.OP_POW
                                ), BinaryExpression.Operator.OP_ADD
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseIfStatement() {
        createAST("""
                if (5 == 5) { 1 } else if (b) c = 2 else \nc = 5
                if (c == 2) "true"
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new IfStatement(
                                null,
                                new BinaryExpression(
                                        null,
                                        new LongLiteralExpression(null, 5),
                                        new LongLiteralExpression(null, 5),
                                        BinaryExpression.Operator.OP_EQUAL
                                ),
                                new BlockStatement(
                                        null,
                                        StatementList.of(
                                                new LongLiteralExpression(null, 1)
                                        )
                                ),
                                new BlockStatement(
                                        null,
                                        StatementList.of(
                                                new IfStatement(
                                                        null,
                                                        new IdentifierExpression(null, "b"),
                                                        new BlockStatement(
                                                                null,
                                                                StatementList.of(
                                                                        new BinaryExpression(
                                                                                null,
                                                                                new IdentifierExpression(null, "c"),
                                                                                new LongLiteralExpression(null, 2),
                                                                                BinaryExpression.Operator.OP_ASSIGN
                                                                        )
                                                                )
                                                        ),
                                                        new BlockStatement(
                                                                null,
                                                                StatementList.of(
                                                                        new BinaryExpression(
                                                                                null,
                                                                                new IdentifierExpression(null, "c"),
                                                                                new LongLiteralExpression(null, 5),
                                                                                BinaryExpression.Operator.OP_ASSIGN
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new IfStatement(
                                null,
                                new BinaryExpression(
                                        null,
                                        new IdentifierExpression(null, "c"),
                                        new LongLiteralExpression(null, 2),
                                        BinaryExpression.Operator.OP_EQUAL
                                ),
                                new BlockStatement(
                                        null,
                                        StatementList.of(
                                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "true")))
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
    public void testParseLoopStatement() {
        createAST("""
                loop {}
                loop a = a + 5
                loop (i < 5) {}
                loop (i < 5, i = i + 1) {}
                loop (let a = 5, let b = 3) {}
                loop (let a = 5, a, print([a])) {}
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new LoopStatement(null, Collections.emptyList(), null, new BlockStatement(null, StatementList.of()), null),
                        new LoopStatement(
                                null, Collections.emptyList(), null,
                                new BlockStatement(null, StatementList.of(
                                        new BinaryExpression(
                                                null,
                                                new IdentifierExpression(null, "a"),
                                                new BinaryExpression(
                                                        null,
                                                        new IdentifierExpression(null, "a"),
                                                        new LongLiteralExpression(null, 5),
                                                        BinaryExpression.Operator.OP_ADD
                                                ),
                                                BinaryExpression.Operator.OP_ASSIGN
                                        )
                                )),
                                null
                        ),
                        new LoopStatement(
                                null, Collections.emptyList(),
                                new BinaryExpression(
                                        null,
                                        new IdentifierExpression(null, "i"),
                                        new LongLiteralExpression(null, 5),
                                        BinaryExpression.Operator.OP_LESS
                                ),
                                new BlockStatement(null, StatementList.of()), null
                        ),
                        new LoopStatement(
                                null, Collections.emptyList(),
                                new BinaryExpression(
                                        null,
                                        new IdentifierExpression(null, "i"),
                                        new LongLiteralExpression(null, 5),
                                        BinaryExpression.Operator.OP_LESS
                                ),
                                new BlockStatement(null, StatementList.of()),
                                new BinaryExpression(
                                        null,
                                        new IdentifierExpression(null, "i"),
                                        new BinaryExpression(
                                                null,
                                                new IdentifierExpression(null, "i"),
                                                new LongLiteralExpression(null, 1),
                                                BinaryExpression.Operator.OP_ADD
                                        ),
                                        BinaryExpression.Operator.OP_ASSIGN
                                )
                        ),
                        new LoopStatement(
                                null,
                                List.of(
                                        new VariableDeclaration(
                                                null,
                                                null,
                                                new IdentifierExpression(null, "a"),
                                                new LongLiteralExpression(null, 5)
                                        ),
                                        new VariableDeclaration(
                                                null,
                                                null,
                                                new IdentifierExpression(null, "b"),
                                                new LongLiteralExpression(null, 3)
                                        )
                                ),
                                null,
                                new BlockStatement(null, StatementList.of()),
                                null
                        ),
                        new LoopStatement(
                                null,
                                List.of(
                                        new VariableDeclaration(
                                                null,
                                                null,
                                                new IdentifierExpression(null, "a"),
                                                new LongLiteralExpression(null, 5)
                                        )
                                ),
                                new IdentifierExpression(null, "a"),
                                new BlockStatement(null, StatementList.of()),
                                new FunctionInvocationExpression(
                                        null,
                                        new IdentifierExpression(null, "print"),
                                        List.of(),
                                        ExpressionList.of(
                                                new ListLiteralExpression(null, ExpressionList.of(new IdentifierExpression(null, "a")))
                                        )
                                )
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseVariableDeclarationMissingInitializer() {
        assertThrows(RuntimeException.class, () -> createAST("let d: double"));
        assertThrows(RuntimeException.class, () -> createAST("let d"));
    }

    @Test
    public void testParseVariableDeclaration() {
        createAST("let d: double = 25");
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, new IdentifierExpression(null, "double")),
                                new IdentifierExpression(null, "d"),
                                new LongLiteralExpression(null, 25)
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseAssignment() {
        createAST("""
                d = 25
                d.a.c.b().a = 25
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new BinaryExpression(
                                null,
                                new IdentifierExpression(null, "d"),
                                new LongLiteralExpression(null, 25),
                                BinaryExpression.Operator.OP_ASSIGN
                        ),
                        new BinaryExpression(
                                null,
                                new MemberAccessExpression(
                                        null,
                                        new IdentifierExpression(null, "a"),
                                        new FunctionInvocationExpression(
                                                null,
                                                new MemberAccessExpression(
                                                        null,
                                                        new IdentifierExpression(null, "b"),
                                                        new MemberAccessExpression(
                                                                null,
                                                                new IdentifierExpression(null, "c"),
                                                                new MemberAccessExpression(
                                                                        null,
                                                                        new IdentifierExpression(null, "a"),
                                                                        new IdentifierExpression(null, "d")
                                                                )
                                                        )
                                                ),
                                                List.of(),
                                                ExpressionList.of()
                                        )
                                ),
                                new LongLiteralExpression(null, 25),
                                BinaryExpression.Operator.OP_ASSIGN
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseInvalidAssignment() {
        assertThrows(RuntimeException.class, () -> createAST("25 = 25"));
        assertThrows(RuntimeException.class, () -> createAST("new Object() = 25"));
    }

    @Test
    public void testParseFunctionDeclaration() {
        createAST("""
                fn foo<T, Z>(self: type, b: long) {
                    fn bar() string {
                        return "hello"
                    }
                    
                    return}
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new FunctionDeclaration(
                                null, null,
                                new IdentifierExpression(null, "foo"),
                                List.of(
                                        new GenericParameterDeclaration(null, "T"),
                                        new GenericParameterDeclaration(null, "Z")
                                ),
                                List.of(
                                        new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, new IdentifierExpression(null, "type")), new IdentifierExpression(null, "self")),
                                        new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, new IdentifierExpression(null, "long")), new IdentifierExpression(null, "b"))
                                ), null,
                                new BlockStatement(
                                        null,
                                        StatementList.of(
                                                new FunctionDeclaration(
                                                        null, null,
                                                        new IdentifierExpression(null, "bar"),
                                                        List.of(), List.of(), new TypeExpression(null, new IdentifierExpression(null, "string")),
                                                        new BlockStatement(
                                                                null,
                                                                StatementList.of(
                                                                        new ReturnStatement(null, new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "hello"))))
                                                                )
                                                        ),
                                                        false
                                                ),
                                                new ReturnStatement(null, null)
                                        )
                                ),
                                false
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseFunctionInvocation() {
        createAST("""
                1 + (foo(1, 2))("hello world")
                bar<|list, double>()()
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new BinaryExpression(
                                null,
                                new LongLiteralExpression(null, 1),
                                new FunctionInvocationExpression(
                                        null,
                                        new ParenthesisedExpression(
                                                null,
                                                new FunctionInvocationExpression(
                                                        null,
                                                        new IdentifierExpression(null, "foo"),
                                                        List.of(),
                                                        ExpressionList.of(
                                                                new LongLiteralExpression(null, 1),
                                                                new LongLiteralExpression(null, 2)
                                                        )
                                                )
                                        ),
                                        List.of(),
                                        ExpressionList.of(
                                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "hello world")))
                                        )
                                ),
                                BinaryExpression.Operator.OP_ADD
                        ),
                        new FunctionInvocationExpression(
                                null,
                                new FunctionInvocationExpression(
                                        null,
                                        new IdentifierExpression(null, "bar"),
                                        List.of(
                                                new TypeExpression(null, new IdentifierExpression(null, "list"), List.of(new TypeExpression(null, new IdentifierExpression(null, "long")))),
                                                new TypeExpression(null, new IdentifierExpression(null, "long"))
                                        ),
                                        ExpressionList.of()
                                ),
                                List.of(),
                                ExpressionList.of()
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseStructDeclaration() {
        createAST("""
                struct Foo<T, A, B> {
                    a: long
                    
                    fn bar(b: string) string return
                    
                    c: double
                }
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new StructDeclaration(
                                null, null,
                                new IdentifierExpression(null, "Foo"),
                                List.of(
                                        new GenericParameterDeclaration(null, "T"),
                                        new GenericParameterDeclaration(null, "A"),
                                        new GenericParameterDeclaration(null, "B")
                                ),
                                List.of(
                                        new StructDeclaration.FieldDeclaration(
                                                null,
                                                new TypeExpression(null, new IdentifierExpression(null, "long")),
                                                new IdentifierExpression(null, "a"),
                                                0
                                        ),
                                        new StructDeclaration.FieldDeclaration(
                                                null,
                                                new TypeExpression(null, new IdentifierExpression(null, "double")),
                                                new IdentifierExpression(null, "c"),
                                                1
                                        )
                                ),
                                List.of(
                                        new FunctionDeclaration(
                                                null, null,
                                                new IdentifierExpression(null, "bar"),
                                                List.of(),
                                                List.of(
                                                        new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, new IdentifierExpression(null, "string")), new IdentifierExpression(null, "b"))
                                                ), new TypeExpression(null, new IdentifierExpression(null, "string")),
                                                new BlockStatement(
                                                        null,
                                                        StatementList.of(
                                                                new ReturnStatement(null, null)
                                                        )
                                                ),
                                                true
                                        )
                                )
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseInvalidStructDeclaration() {
        assertThrows(RuntimeException.class, () -> createAST("""
                struct d {
                    5
                }
                """));
        assertThrows(RuntimeException.class, () -> createAST("""
                struct d {
                    fn test(self: long) {}
                }
                """));
    }

    @Test
    public void testParseObjectCreation() {
        createAST("""
                new Foo(1, 2, 3)
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new ObjectCreationExpression(
                                null,
                                new IdentifierExpression(null, "Foo"),
                                List.of(), ExpressionList.of(
                                new LongLiteralExpression(null, 1),
                                new LongLiteralExpression(null, 2),
                                new LongLiteralExpression(null, 3)
                        )
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseMemberAccess() {
        createAST("""
                a.b().c().d
                """);
        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new MemberAccessExpression(
                                null,
                                new IdentifierExpression(null, "d"),
                                new FunctionInvocationExpression(
                                        null,
                                        new MemberAccessExpression(
                                                null,
                                                new IdentifierExpression(null, "c"),
                                                new FunctionInvocationExpression(
                                                        null,
                                                        new MemberAccessExpression(
                                                                null,
                                                                new IdentifierExpression(null, "b"),
                                                                new IdentifierExpression(null, "a")
                                                        ),
                                                        List.of(),
                                                        ExpressionList.of()
                                                )
                                        ),
                                        List.of(),
                                        ExpressionList.of()
                                )
                        )
                )
                ),
                this.unit
        );
    }

    @Test
    public void testParseUseStatements() {
        createAST("""
                use "foo/bar" { Test }
                use "test" { thing, otherThing }
                                
                if(true){
                    use "../foo/../bar" { Test2 }
                }
                """);

        assertEquals(
                new SourceFileUnit(
                        "main", StatementList.of(
                        new UseStatement(
                                null,
                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "foo/bar"))),
                                List.of(new IdentifierExpression(null, "Test"))
                        ),
                        new UseStatement(
                                null,
                                new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "test"))),
                                List.of(new IdentifierExpression(null, "thing"), new IdentifierExpression(null, "otherThing"))
                        ),
                        new IfStatement(
                                null,
                                new BooleanLiteralExpression(null, true),
                                new BlockStatement(
                                        null,
                                        StatementList.of(
                                                new UseStatement(
                                                        null,
                                                        new StringLiteralExpression(null, List.of(StringLiteralExpression.stringPart(null, "../foo/../bar"))),
                                                        List.of(new IdentifierExpression(null, "Test2"))
                                                )
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
    public void testParseInvalidUseStatement() {
        assertThrows(RuntimeException.class, () -> createAST("use foo { Test }"));
        assertThrows(RuntimeException.class, () -> createAST("use \"foo\" {  }"));
        assertThrows(RuntimeException.class, () -> createAST("use {  }"));
        assertThrows(RuntimeException.class, () -> createAST("use ddd"));
    }

    @Test
    public void testEmptyGenericParameterList() {
        assertThrows(RuntimeException.class, () -> createAST("let a: T<> = 5"));
    }

    @Test
    public void testParseParallel() {
        var sources = IntStream.range(0, 10000)
                .mapToObj(i -> new InMemorySource("main" + i, "let a = " + i))
                .toList();
        var results = Parser.parseParallel(sources);

        assertEquals(sources.size(), results.size());
        for (var result : results) {
            if (result.hasProblems())
                fail(result.getProblems().prettyPrint(false));

            assertEquals(
                    new SourceFileUnit(
                            "main", StatementList.of(
                            new VariableDeclaration(
                                    null,
                                    null,
                                    new IdentifierExpression(null, "a"),
                                    new LongLiteralExpression(null, Long.parseLong(result.getSource().getModuleName().substring(4)))
                            )
                    )
                    ),
                    result.getUnit()
            );
        }
    }

    @Test
    public void testAllowNewlines() {
        createAST("""
                use
                "test/b"
                {
                a
                ,
                b
                }
                                
                a
                +
                a
                -
                b / d
                * c ^ d
                fn
                a
                <
                a
                ,
                b
                ,
                c
                >
                (a: a,
                 b: b
                 ) a
                 {
                 a =
                 b
                 a.b.c
                 (a)
                    if
                    (test(a,b))
                    {}else
                    
                    {
                        print
                        (
                        a
                        )
                        return
                        a
                    }
                 }
                loop
                (
                let
                a = 5
                ,
                b <
                5
                ,
                c = c+1
                )
                {
                }
                """);
    }
}
