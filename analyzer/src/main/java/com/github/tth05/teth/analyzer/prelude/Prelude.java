package com.github.tth05.teth.analyzer.prelude;

import com.github.tth05.teth.lang.parser.Parser;
import com.github.tth05.teth.lang.parser.StatementList;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IHasName;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.parser.ast.StructDeclaration;
import com.github.tth05.teth.lang.source.InMemorySource;
import com.github.tth05.teth.lang.span.Span;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Prelude {

    private static final String PRELUDE_TEXT = """
            struct intrinsic long {
                fn intrinsic toDouble() double
                fn intrinsic toString() string
            }
            struct intrinsic double {
                fn intrinsic toLong() long
            }
            struct intrinsic bool {}
            struct intrinsic string {
                fn intrinsic concat(other: string) string
            }
            struct intrinsic list<T> {
                fn intrinsic get(index: long) T
                fn intrinsic set(index: long, value: T)
                fn intrinsic add(value: T)
                fn intrinsic size() long
            }
            struct intrinsic any {}
                        
            fn intrinsic print(arg: any)
            fn intrinsic stringify(arg: any) string
            fn intrinsic nanoTime() long
            """;
    private static final Map<Span, StructDeclaration> PRELUDE_STRUCTS = new HashMap<>();
    private static final Map<Span, FunctionDeclaration> PRELUDE_FUNCTIONS = new HashMap<>();
    static {
        var result = Parser.parse(new InMemorySource("prelude", PRELUDE_TEXT), true);
        if (result.hasProblems())
            throw new RuntimeException("Failed to parse prelude\n" + result.getProblems().prettyPrint(false));

        for (var statement : result.getUnit().getStatements()) {
            if (statement instanceof StructDeclaration structDeclaration)
                PRELUDE_STRUCTS.put(((IHasName) statement).getNameExpr().getSpan(), structDeclaration);
            else if (statement instanceof FunctionDeclaration functionDeclaration)
                PRELUDE_FUNCTIONS.put(((IHasName) statement).getNameExpr().getSpan(), functionDeclaration);
            else
                throw new RuntimeException();
        }
    }

    public static FunctionDeclaration getGlobalFunction(Span name) {
        if (name == null)
            return null;

        return PRELUDE_FUNCTIONS.get(name);
    }

    public static FunctionDeclaration[] getGlobalFunctions() {
        return PRELUDE_FUNCTIONS.values().toArray(new FunctionDeclaration[0]);
    }

    public static StatementList getAllDeclarations() {
        var list = StatementList.of();
        list.addAll(PRELUDE_STRUCTS.values());
        list.addAll(PRELUDE_FUNCTIONS.values());
        return list;
    }

    public static StructDeclaration getStructForTypeName(Span name) {
        if (name == null)
            throw new IllegalArgumentException();

        var struct = PRELUDE_STRUCTS.get(name);
        if (struct == null)
            throw new IllegalArgumentException();

        return struct;
    }

    public static StructDeclaration[] getGlobalStructs() {
        return PRELUDE_STRUCTS.values().toArray(new StructDeclaration[0]);
    }

    public static void injectStatements(List<Statement> statements) {
        for (var function : PRELUDE_FUNCTIONS.values())
            statements.add(0, function);
        for (var struct : PRELUDE_STRUCTS.values())
            statements.add(0, struct);
    }
    /**
     * Quick accessors
     */
    private static StructDeclaration LONG_STRUCT;

    public static StructDeclaration getLongStruct() {
        if (LONG_STRUCT == null)
            LONG_STRUCT = Objects.requireNonNull(getStructForTypeName(Span.fromString("long")));
        return LONG_STRUCT;
    }
    private static StructDeclaration DOUBLE_STRUCT;

    public static StructDeclaration getDoubleStruct() {
        if (DOUBLE_STRUCT == null)
            DOUBLE_STRUCT = Objects.requireNonNull(getStructForTypeName(Span.fromString("double")));
        return DOUBLE_STRUCT;
    }
    private static StructDeclaration BOOL_STRUCT;

    public static StructDeclaration getBoolStruct() {
        if (BOOL_STRUCT == null)
            BOOL_STRUCT = Objects.requireNonNull(getStructForTypeName(Span.fromString("bool")));
        return BOOL_STRUCT;
    }
    private static StructDeclaration STRING_STRUCT;

    public static StructDeclaration getStringStruct() {
        if (STRING_STRUCT == null)
            STRING_STRUCT = Objects.requireNonNull(getStructForTypeName(Span.fromString("string")));
        return STRING_STRUCT;
    }
    private static StructDeclaration LIST_STRUCT;

    public static StructDeclaration getListStruct() {
        if (LIST_STRUCT == null)
            LIST_STRUCT = Objects.requireNonNull(getStructForTypeName(Span.fromString("list")));
        return LIST_STRUCT;
    }
    private static StructDeclaration ANY_STRUCT;

    public static StructDeclaration getAnyStruct() {
        if (ANY_STRUCT == null)
            ANY_STRUCT = Objects.requireNonNull(getStructForTypeName(Span.fromString("any")));
        return ANY_STRUCT;
    }
}
