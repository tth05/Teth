package com.github.tth05.teth.analyzer.prelude;

import com.github.tth05.teth.lang.parser.ast.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Prelude {

    /**
     * Structs
     */

    public static final StructDeclaration LONG_STRUCT_DECLARATION = new StructDeclaration(
            null, null,
            new IdentifierExpression(null, "long"),
            List.of(),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("toString", true, type("string"))
            ),
            true
    );

    public static final StructDeclaration DOUBLE_STRUCT_DECLARATION = new StructDeclaration(
            null, null,
            new IdentifierExpression(null, "double"),
            List.of(),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("toLong", true, type("long"))
            ),
            true
    );
    public static final StructDeclaration BOOLEAN_STRUCT_DECLARATION = new StructDeclaration(
            null, null,
            new IdentifierExpression(null, "bool"),
            List.of(),
            List.of(),
            List.of(),
            true
    );
    public static final StructDeclaration STRING_STRUCT_DECLARATION = new StructDeclaration(
            null, null,
            new IdentifierExpression(null, "string"),
            List.of(),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("concat", true, type("string"), type("string"))
            ),
            true
    );
    public static final StructDeclaration LIST_STRUCT_DECLARATION = new StructDeclaration(
            null, null,
            new IdentifierExpression(null, "list"),
            List.of(new GenericParameterDeclaration(null, "T")),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("size", true, type("long")),
                    createFakeFunctionDeclaration("get", true, type("T"), type("long")),
                    createFakeFunctionDeclaration("set", true, null, type("long"), type("T")),
                    createFakeFunctionDeclaration("add", true, null, type("T"))
            ),
            true
    );

    public static final StructDeclaration ANY_STRUCT_DECLARATION = new StructDeclaration(
            null, null,
            new IdentifierExpression(null, "any"),
            List.of(),
            List.of(),
            List.of(),
            true
    );

    /**
     * Functions
     */

    private static final FunctionDeclaration PRINT_FUNCTION = createFakeFunctionDeclaration(
            "print",
            false,
            null,
            type("any")
    );
    private static final FunctionDeclaration STRINGIFY_FUNCTION = createFakeFunctionDeclaration(
            "stringify",
            false,
            type("string"),
            type("any")
    );
    private static final FunctionDeclaration NANO_TIME_FUNCTION = createFakeFunctionDeclaration(
            "nanoTime",
            false,
            type("long")
    );

    public static FunctionDeclaration getGlobalFunction(String name) {
        if (name == null)
            return null;

        return switch (name) {
            case "print" -> PRINT_FUNCTION;
            case "stringify" -> STRINGIFY_FUNCTION;
            case "nanoTime" -> NANO_TIME_FUNCTION;
            default -> null;
        };
    }

    public static FunctionDeclaration[] getGlobalFunctions() {
        return new FunctionDeclaration[]{
                PRINT_FUNCTION,
                STRINGIFY_FUNCTION,
                NANO_TIME_FUNCTION
        };
    }

    public static boolean isBuiltInTypeName(String name) {
        if (name == null)
            return false;

        return switch (name) {
            case "long", "double", "bool", "string", "list", "any" -> true;
            default -> false;
        };
    }

    public static StructDeclaration getStructForTypeName(String name) {
        return switch (name) {
            case "long" -> LONG_STRUCT_DECLARATION;
            case "double" -> DOUBLE_STRUCT_DECLARATION;
            case "bool" -> BOOLEAN_STRUCT_DECLARATION;
            case "string" -> STRING_STRUCT_DECLARATION;
            case "list" -> LIST_STRUCT_DECLARATION;
            case "any" -> ANY_STRUCT_DECLARATION;
            default -> throw new IllegalArgumentException();
        };
    }

    public static StructDeclaration[] getGlobalStructs() {
        return new StructDeclaration[]{
                LONG_STRUCT_DECLARATION,
                DOUBLE_STRUCT_DECLARATION,
                BOOLEAN_STRUCT_DECLARATION,
                STRING_STRUCT_DECLARATION,
                LIST_STRUCT_DECLARATION,
                ANY_STRUCT_DECLARATION
        };
    }

    public static void injectStatements(List<Statement> statements) {
        for (var function : getGlobalFunctions())
            statements.add(0, function);
        for (var struct : getGlobalStructs())
            statements.add(0, struct);
    }

    private static TypeExpression type(String name, TypeExpression... params) {
        if (params.length == 0)
            return new TypeExpression(null, new IdentifierExpression(null, name));
        return new TypeExpression(null, new IdentifierExpression(null, name), Arrays.asList(params));
    }

    private static FunctionDeclaration createFakeFunctionDeclaration(String name, boolean instanceFunction, TypeExpression returnType, TypeExpression... parameters) {
        return new FunctionDeclaration(
                null, null,
                new IdentifierExpression(null, name),
                List.of(),
                IntStream.range(0, parameters.length).mapToObj(i -> new FunctionDeclaration.ParameterDeclaration(
                        null,
                        parameters[i],
                        new IdentifierExpression(null, "arg" + i)
                )).toList(),
                returnType,
                null,
                instanceFunction, true
        );
    }
}
