package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.AbstractParserTest;
import com.github.tth05.teth.lang.parser.ast.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest extends AbstractParserTest {

    @Test
    public void testParseString() {
        createAST("\"A string!\"");
        assertEquals(new SourceFileUnit(
                List.of(
                        new StringLiteralExpression(null, "A string!")
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseBoolean() {
        createAST("true");
        assertEquals(new SourceFileUnit(
                List.of(
                        new BooleanLiteralExpression(null, true)
                )
        ), this.unit);
        assertStreamsEmpty();

        createAST("false");
        assertEquals(new SourceFileUnit(
                List.of(
                        new BooleanLiteralExpression(null, false)
                )
        ), this.unit);
        assertStreamsEmpty();
    }

    @Test
    public void testParseListLiteral() {
        createAST("[5,6,7]");
        assertEquals(new SourceFileUnit(
                List.of(
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
        assertStreamsEmpty();
    }

    @Test
    public void testParseTypes() {
        //TODO: Test parsing of generic types
        createAST("""
                let l: long = 5
                let l: double = 5
                let l: string = 5
                let l: boolean = 5
                let l: any = 5
                let l: function = 5
                let l: long[] = 5
                """);
        assertEquals(new SourceFileUnit(
                List.of(
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.fromName("long")),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        ),
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.fromName("double")),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        ),
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.fromName("string")),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        ),
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.fromName("boolean")),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        ),
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.fromName("any")),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        ),
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.fromName("function")),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        ),
                        new VariableDeclaration(
                                null,
                                new TypeExpression(null, Type.list(Type.LONG)),
                                new IdentifierExpression(null, "l"),
                                new LongLiteralExpression(null, 5)
                        )
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
                                null,
                                new UnaryExpression(
                                        null,
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
        assertStreamsEmpty();
    }

    @Test
    public void testParseBinaryExpression() {
        createAST("1+1-1*1/1==1<1<=1>1>=1!=1^(a=1)");
        assertEquals(new SourceFileUnit(
                List.of(
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
                                                        new VariableAssignmentExpression(
                                                                null,
                                                                new IdentifierExpression(null, "a"),
                                                                new LongLiteralExpression(null, 1)
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

        createAST("a && b || c && d");
        assertEquals(new SourceFileUnit(
                List.of(
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
        assertStreamsEmpty();
    }

    @Test
    public void testParseMathExpression() {
        createAST("1 - 1 + (5 * 6 + 1 + 3 * 2^2)^(100 + 1)");
        assertEquals(
                new SourceFileUnit(
                        List.of(
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
                                                ),
                                                new BinaryExpression(
                                                        null,
                                                        new LongLiteralExpression(null, 100),
                                                        new LongLiteralExpression(null, 1),
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
                                                                                new VariableAssignmentExpression(
                                                                                        null,
                                                                                        new IdentifierExpression(null, "c"),
                                                                                        new LongLiteralExpression(null, 2)
                                                                                )
                                                                        )
                                                                ),
                                                                new BlockStatement(
                                                                        null,
                                                                        StatementList.of(
                                                                                new VariableAssignmentExpression(
                                                                                        null,
                                                                                        new IdentifierExpression(null, "c"),
                                                                                        new LongLiteralExpression(null, 5)
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
                                                        new StringLiteralExpression(null, "true")
                                                )
                                        ),
                                        null
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
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
                        List.of(
                                new LoopStatement(null, Collections.emptyList(), null, new BlockStatement(null, StatementList.of()), null),
                                new LoopStatement(
                                        null, Collections.emptyList(), null,
                                        new BlockStatement(null, StatementList.of(
                                                new VariableAssignmentExpression(
                                                        null,
                                                        new IdentifierExpression(null, "a"),
                                                        new BinaryExpression(
                                                                null,
                                                                new IdentifierExpression(null, "a"),
                                                                new LongLiteralExpression(null, 5),
                                                                BinaryExpression.Operator.OP_ADD
                                                        )
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
                                        new VariableAssignmentExpression(
                                                null,
                                                new IdentifierExpression(null, "i"),
                                                new BinaryExpression(
                                                        null,
                                                        new IdentifierExpression(null, "i"),
                                                        new LongLiteralExpression(null, 1),
                                                        BinaryExpression.Operator.OP_ADD
                                                )
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
                                                ExpressionList.of(
                                                        new ListLiteralExpression(null, ExpressionList.of(new IdentifierExpression(null, "a")))
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
    public void testParseVariableDeclarationMissingInitializer() {
        assertThrows(RuntimeException.class, () -> createAST("let d: double"));
        assertThrows(RuntimeException.class, () -> createAST("let d"));
    }

    @Test
    public void testParseVariableDeclaration() {
        createAST("let d: double = 25");
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new VariableDeclaration(
                                        null,
                                        new TypeExpression(null, Type.fromName("double")),
                                        new IdentifierExpression(null, "d"),
                                        new LongLiteralExpression(null, 25)
                                )
                        )
                ),
                this.unit
        );
        assertStreamsEmpty();
    }

    @Test
    public void testParseAssignment() {
        createAST("""
                d = 25
                d.a.c.b().a = 25
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new VariableAssignmentExpression(
                                        null,
                                        new IdentifierExpression(null, "d"),
                                        new LongLiteralExpression(null, 25)
                                ),
                                new VariableAssignmentExpression(
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
                                                        ExpressionList.of()
                                                )
                                        ),
                                        new LongLiteralExpression(null, 25)
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
                fn foo(self: type, b: long) {
                    fn bar() string {
                        return "hello"
                    }
                    
                    return}
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new FunctionDeclaration(
                                        null,
                                        new IdentifierExpression(null, "foo"),
                                        List.of(),
                                        List.of(
                                                new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, Type.fromName("type")), new IdentifierExpression(null, "self")),
                                                new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, Type.fromName("long")), new IdentifierExpression(null, "b"))
                                        ), null,
                                        new BlockStatement(
                                                null,
                                                StatementList.of(
                                                        new FunctionDeclaration(
                                                                null,
                                                                new IdentifierExpression(null, "bar"),
                                                                List.of(), List.of(), new TypeExpression(null, Type.fromName("string")),
                                                                new BlockStatement(
                                                                        null,
                                                                        StatementList.of(
                                                                                new ReturnStatement(null, new StringLiteralExpression(null, "hello"))
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
                                        null,
                                        new LongLiteralExpression(null, 1),
                                        new FunctionInvocationExpression(
                                                null,
                                                new FunctionInvocationExpression(
                                                        null,
                                                        new IdentifierExpression(null, "foo"),
                                                        ExpressionList.of(
                                                                new LongLiteralExpression(null, 1),
                                                                new LongLiteralExpression(null, 2)
                                                        )
                                                ),
                                                ExpressionList.of(
                                                        new StringLiteralExpression(null, "hello world")
                                                )
                                        ),
                                        BinaryExpression.Operator.OP_ADD
                                ),
                                new FunctionInvocationExpression(
                                        null,
                                        new FunctionInvocationExpression(
                                                null,
                                                new IdentifierExpression(null, "bar"),
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
    public void testParseStructDeclaration() {
        createAST("""
                struct Foo {
                    a: long
                    
                    fn bar(b: string) string return
                    
                    c: double
                }
                """);
        assertEquals(
                new SourceFileUnit(
                        List.of(
                                new StructDeclaration(
                                        null,
                                        new IdentifierExpression(null, "Foo"),
                                        List.of(
                                                new StructDeclaration.FieldDeclaration(
                                                        null,
                                                        new TypeExpression(null, Type.LONG),
                                                        new IdentifierExpression(null, "a"),
                                                        0
                                                ),
                                                new StructDeclaration.FieldDeclaration(
                                                        null,
                                                        new TypeExpression(null, Type.DOUBLE),
                                                        new IdentifierExpression(null, "c"),
                                                        1
                                                )
                                        ),
                                        List.of(
                                                new FunctionDeclaration(
                                                        null,
                                                        new IdentifierExpression(null, "bar"),
                                                        List.of(),
                                                        List.of(
                                                                new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, Type.STRING), new IdentifierExpression(null, "b"))
                                                        ), new TypeExpression(null, Type.STRING),
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
        assertStreamsEmpty();
    }

    @Test
    public void testParseInvalidStructDeclaration() {
        assertThrows(RuntimeException.class, () -> createAST("""
                struct d {
                    a: long
                    a: long
                }
                """));
        assertThrows(RuntimeException.class, () -> createAST("""
                struct d {
                    a: long
                    fn a() {}
                }
                """));
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
                        List.of(
                                new ObjectCreationExpression(
                                        null,
                                        new IdentifierExpression(null, "Foo"),
                                        ExpressionList.of(
                                                new LongLiteralExpression(null, 1),
                                                new LongLiteralExpression(null, 2),
                                                new LongLiteralExpression(null, 3)
                                        )
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

    @Test
    public void testAllowNewlines() {
        createAST("""
                a
                +
                a
                -
                b / d 
                * c ^ d
                fn 
                a
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
        assertStreamsEmpty();
    }
}
