package com.github.tth05.teth.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerTypeResolverTest extends AbstractAnalyzerTest {

    @Test
    public void testBinaryExpressionLong() {
        var problems = analyze("1+1");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testBinaryExpressionImplicitDouble() {
        var problems = analyze("1+1.0");

        assertTrue(problems.isEmpty());
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

        problems = analyze("-56.0");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testUnaryExpressionBoolean() {
        var problems = analyze("!!(true)");

        assertTrue(problems.isEmpty());
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

        problems = analyze("[-56, 5, 1]");

        assertTrue(problems.isEmpty());

        problems = analyze("[\"5\", \"1\", \"2\"]");

        assertTrue(problems.isEmpty());

        problems = analyze("[[5]]");

        assertTrue(problems.isEmpty());
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

    @Test
    public void testVariableDeclaration() {
        var problems = analyze("let a: list<list<long>> = [[1], [2], [3]]");

        assertTrue(problems.isEmpty());

        problems = analyze("let a = [1, 2, 3]\n a=[5]");

        assertTrue(problems.isEmpty());

        problems = analyze("let d: double = 5.0");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testVariableDeclarationUnknownType() {
        var problems = analyze("let a: list<list<longg>> = 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unknown type longg", problems.get(0).message());

        problems = analyze("let a: test = 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unknown type test", problems.get(0).message());
    }

    @Test
    public void testVariableDeclarationIncompatibleTypes() {
        var problems = analyze("let a: list<long> = [\"\"]");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot assign value of type list<string> to variable of type list<long>", problems.get(0).message());

        problems = analyze("let d: double = 6");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot assign value of type long to variable of type double", problems.get(0).message());
    }

    @Test
    public void testStringInterpolation() {
        var problems = analyze("\"Hello, {\"World\"}!\"");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testIncorrectTypeStringInterpolation() {
        var problems = analyze("\"Hello, {5}!\"");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("String literal part must be a string", problems.get(0).message());
    }

    @Test
    public void testIfStatementCondition() {
        var problems = analyze("if(true) 5");

        assertTrue(problems.isEmpty());

        problems = analyze("if(true == false) 5");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testIfStatementConditionIncorrectType() {
        var problems = analyze("if(5) 5");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Condition of if statement must be a bool", problems.get(0).message());
    }

    @Test
    public void testLoopStatementCondition() {
        var problems = analyze("loop (true) {}");

        assertTrue(problems.isEmpty());

        problems = analyze("loop (let a = 5, a < 5) {}");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testLoopStatementConditionIncorrectType() {
        var problems = analyze("loop (5) {}");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Condition of loop statement must be a bool", problems.get(0).message());
    }

    @Test
    public void testAssignToVariable() {
        var problems = analyze("let a = 0\n a = 5");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testAssignToVariableIncompatibleTypes() {
        var problems = analyze("let a = 0 \n a=5.0");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot assign expression of type double to variable of type long", problems.get(0).message());
    }

    @Test
    public void testReturn() {
        var problems = analyze("fn f() long {return 5}");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testReturnIncompatibleTypes() {
        var problems = analyze("fn f(){return 5}");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot return long from function returning void", problems.get(0).message());

        problems = analyze("fn f() double {return true}");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot return bool from function returning double", problems.get(0).message());
    }

    @Test
    public void testFunctionReturnType() {
        var problems = analyze("fn f() long {return 5}let l: long = f()");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testFunctionReturnTypeIncompatible() {
        var problems = analyze("fn f(){}let d: double = f()");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot assign value of type void to variable of type double", problems.get(0).message());
    }

    @Test
    public void testFunctionParameterTypes() {
        var problems = analyze("fn f() long {return 5}f()\nfn b(l : long) {}b(5)");

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testFunctionParameterTypesMismatch() {
        var problems = analyze("fn f(a: long, s: string, l: long) {}f(5, \"\", 5.0)");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Parameter type mismatch. Expected long, got double", problems.get(0).message());
    }

    @Test
    public void testStructUsingSelfParameter() {
        var problems = analyze("""
                struct S {
                    a: long
                    fn t() {
                        self.a = 5
                        let l: long = self.a
                    }
                }
                """);

        System.out.println(problems.prettyPrint(true));
        assertTrue(problems.isEmpty());
    }

    @Test
    public void testStructUsingSelfParameterNotAvailable() {
        var problems = analyze("""
                fn t() {
                    self.a = 5
                }
                """);

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Unresolved identifier", problems.get(0).message());
    }

    @Test
    public void testObjectCreation() {
        var problems = analyze("struct a {} let b: a = new a()");

        assertTrue(problems.isEmpty());

        problems = analyze("""
                struct a {}
                struct b {
                    a: a
                }
                                
                let d: a = new b(new a()).a
                """);

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testObjectCreationParameterTypesMismatch() {
        var problems = analyze("struct a {b:long} new a(5.0)");

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Parameter type mismatch. Expected long, got double", problems.get(0).message());
    }

    @Test
    public void testFunctionInferGenericParam() {
        var problems = analyze("""
                fn test<T>(t: T) T {
                return t
                }
                                
                let a: long = test(5)
                print([a])
                """);

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testFunctionExplicitGenericParam() {
        var problems = analyze("""
                fn test<T>(t: T) T {
                return t
                }
                                
                let a: long = test<|long>(5)
                print([a])
                """);

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testFunctionInferNestedGenericParam() {
        var problems = analyze("""
                let a: list<list<long>> = test(5)
                print([a])
                                
                fn test<T>(t: T) list<list<T>> {
                    let l = [[t]]
                    return l
                }
                """);

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testFunctionExplicitGenericTypeMismatch() {
        var problems = analyze("""
                fn test<T>(t: T) T {
                    return t
                }
                                
                let a: long = test<|double>(5)
                """);

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Parameter type mismatch. Expected double, got long", problems.get(0).message());
    }

    @Test
    public void testFunctionReturningIncorrectGenericType() {
        var problems = analyze("""
                fn test<T>(t: T) list<list<T>> {
                    let l = [t]
                    return l
                }
                """);

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot return list<T> from function returning list<list<T>>", problems.get(0).message());
    }

    @Test
    public void testFunctionResolvedToCorrectNonGenericType() {
        var problems = analyze("""
                let a: list<long> = test(5)
                print([a])
                                
                fn test<T>(t: T) list<list<T>> {
                    let l = [[t]]
                    return l
                }
                """);

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot assign value of type list<list<long>> to variable of type list<long>", problems.get(0).message());
    }

    @Test
    public void testTypesAreNotJustComparedByName() {
        var problems = analyze("""
                struct T {}
                                
                fn test() T {return new T()}
                                
                fn test2<T>() T {return test()}
                """);

        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals("Cannot return T from function returning T", problems.get(0).message());
    }
}
