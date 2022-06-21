package com.github.tth05.teth.lang.stdlib;

import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IdentifierExpression;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.parser.ast.TypeExpression;

import java.util.ArrayList;

public class StandardLibrary {

    private static FunctionDeclaration[] LONG_FUNCTIONS = null;
    private static FunctionDeclaration[] STRING_FUNCTIONS = null;
    private static FunctionDeclaration[] LIST_FUNCTIONS = null;
    private static FunctionDeclaration[] GLOBAL_FUNCTIONS = null;

    public static Statement getMemberOfType(Type type, String memberName) {
        var members = getMembersOfType(type);
        for (var member : members) {
            var name = switch (member) {
                case FunctionDeclaration decl -> decl.getNameExpr().getValue();
                default -> throw new IllegalStateException();
            };

            // ew
            if (name.split("\\.")[1].equals(memberName))
                return member;
        }

        return null;
    }

    public static Statement[] getMembersOfType(Type type) {
        if (type == Type.LONG) {
            if (LONG_FUNCTIONS == null)
                LONG_FUNCTIONS = new FunctionDeclaration[]{
                        createFakeFunctionDeclaration("long.toString", Type.STRING),
                        createFakeFunctionDeclaration("long.toBinaryString", Type.STRING),
                };

            return LONG_FUNCTIONS;
        } else if (type == Type.STRING) {
            if (STRING_FUNCTIONS == null)
                STRING_FUNCTIONS = new FunctionDeclaration[]{createFakeFunctionDeclaration("string.len", Type.LONG)};

            return STRING_FUNCTIONS;
        } else if (type.isList()) {
            if (LIST_FUNCTIONS == null)
                LIST_FUNCTIONS = new FunctionDeclaration[]{
                        createFakeFunctionDeclaration("list.len", Type.LONG),
                        createFakeFunctionDeclaration("list.add", Type.VOID, Type.ANY),
                        createFakeFunctionDeclaration("list.remove", Type.VOID, Type.LONG),
                        createFakeFunctionDeclaration("list.get", Type.ANY, Type.LONG),
                };

            return LIST_FUNCTIONS;
        }

        return new Statement[0];
    }

    public static Statement getGlobalFunction(String value) {
        for (var function : getGlobalFunctions()) {
            if (((FunctionDeclaration) function).getNameExpr().getValue().equals(value))
                return function;
        }

        return null;
    }

    public static Statement[] getGlobalFunctions() {
        if (GLOBAL_FUNCTIONS == null) {
            GLOBAL_FUNCTIONS = new FunctionDeclaration[]{
                    createFakeFunctionDeclaration("print", Type.VOID, new Type(Type.ANY)),
                    createFakeFunctionDeclaration("println", Type.VOID, new Type(Type.ANY)),
            };
        }

        return GLOBAL_FUNCTIONS;
    }

    private static FunctionDeclaration createFakeFunctionDeclaration(String name, Type returnType, Type... argTypes) {
        var args = new ArrayList<FunctionDeclaration.ParameterDeclaration>(argTypes.length);
        for (int i = 0; i < argTypes.length; i++)
            args.add(new FunctionDeclaration.ParameterDeclaration(null, new TypeExpression(null, argTypes[i]), new IdentifierExpression(null, "arg" + i), i));

        return new FunctionDeclaration(null,
                new IdentifierExpression(null, name),
                new TypeExpression(null, returnType),
                args, null, true
        );
    }

    public static boolean isBuiltinType(Type type) {
        // Type constants will be removed once structs exist
        return type.isNumber() ||
               type == Type.STRING ||
               type == Type.BOOLEAN ||
               type == Type.FUNCTION ||
               type == Type.VOID ||
               type == Type.ANY ||
               (type.isList() && isBuiltinType(type.getInnerType()));
    }
}
