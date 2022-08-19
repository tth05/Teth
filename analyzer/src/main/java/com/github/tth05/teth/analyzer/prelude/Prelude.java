package com.github.tth05.teth.analyzer.prelude;

import com.github.tth05.teth.lang.parser.ast.*;

import java.util.Arrays;
import java.util.List;

public class Prelude {

    /**
     * Structs
     */

    public static final StructDeclaration LONG_STRUCT_DECLARATION = new StructDeclaration(
            null,
            new IdentifierExpression(null, "long"),
            List.of(),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("toString", true, type("string"))
            )
    );

    public static final StructDeclaration DOUBLE_STRUCT_DECLARATION = new StructDeclaration(
            null,
            new IdentifierExpression(null, "double"),
            List.of(),
            List.of(),
            List.of()
    );
    public static final StructDeclaration BOOLEAN_STRUCT_DECLARATION = new StructDeclaration(
            null,
            new IdentifierExpression(null, "bool"),
            List.of(),
            List.of(),
            List.of()
    );
    public static final StructDeclaration STRING_STRUCT_DECLARATION = new StructDeclaration(
            null,
            new IdentifierExpression(null, "string"),
            List.of(),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("concat", true, type("string"), type("string"))
            )
    );
    public static final StructDeclaration LIST_STRUCT_DECLARATION = new StructDeclaration(
            null,
            new IdentifierExpression(null, "list"),
            List.of(new GenericParameterDeclaration(null, "T")),
            List.of(),
            List.of(
                    createFakeFunctionDeclaration("get", true, type("T"), type("long")),
                    createFakeFunctionDeclaration("set", true, null, type("long"), type("T")),
                    createFakeFunctionDeclaration("add", true, null, type("T"))
            )
    );

    public static final StructDeclaration ANY_STRUCT_DECLARATION = new StructDeclaration(
            null,
            new IdentifierExpression(null, "any"),
            List.of(),
            List.of(),
            List.of()
    );

    /**
     * Functions
     */

    private static final FunctionDeclaration PRINT_FUNCTION = createFakeFunctionDeclaration(
            "print",
            false,
            null,
            type("list", type("any"))
    );

    public static FunctionDeclaration getGlobalFunction(String name) {
        return switch (name) {
            case "print" -> PRINT_FUNCTION;
            default -> null;
        };
    }

    public static boolean isBuiltInTypeName(String name) {
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

    public static void injectStatements(List<Statement> statements) {
        statements.add(0, LONG_STRUCT_DECLARATION);
        statements.add(0, DOUBLE_STRUCT_DECLARATION);
        statements.add(0, BOOLEAN_STRUCT_DECLARATION);
        statements.add(0, STRING_STRUCT_DECLARATION);
        statements.add(0, LIST_STRUCT_DECLARATION);
        statements.add(0, ANY_STRUCT_DECLARATION);
        statements.add(0, PRINT_FUNCTION);
    }

    private static TypeExpression type(String name, TypeExpression... params) {
        if (params.length == 0)
            return new TypeExpression(null, name);
        return new TypeExpression(null, name, Arrays.asList(params));
    }

    private static FunctionDeclaration createFakeFunctionDeclaration(String name, boolean instanceFunction, TypeExpression returnType, TypeExpression... parameters) {
        return new FunctionDeclaration(
                null,
                new IdentifierExpression(null, name),
                List.of(),
                Arrays.stream(parameters).map(t -> new FunctionDeclaration.ParameterDeclaration(
                        null,
                        t,
                        new IdentifierExpression(null, "arg")
                )).toList(),
                returnType,
                null,
                instanceFunction, true
        );
    }
}