package com.github.tth05.teth.interpreter.environment;

import com.github.tth05.teth.interpreter.Interpreter;
import com.github.tth05.teth.interpreter.InterpreterException;
import com.github.tth05.teth.interpreter.values.*;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.span.ISpan;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Environment {

    private final Map<String, IFunction> topLevelFunctions = new HashMap<>();
    {
        addTopLevelFunction("print", IntrinsicFunction.create("print", (parameters) -> {
            for (int i = 0; i < parameters.length; i++) {
                System.out.print(parameters[i].getDebugString() + (i < parameters.length - 1 ? " " : ""));
            }
            System.out.println();
            return null;
        }, true, Type.ANY));
        addTopLevelFunction("__long_toString", IntrinsicFunction.create(
                "__long_toString",
                (parameters) -> new StringValue(((LongValue) parameters[0]).getValue() + ""),
                false,
                Type.LONG
        ));
        addTopLevelFunction("__long_toBinaryString", IntrinsicFunction.create(
                "__long_toBinaryString",
                (parameters) -> new StringValue(Long.toBinaryString(((LongValue) parameters[0]).getValue())),
                false,
                Type.LONG
        ));
        addTopLevelFunction("__string_length", IntrinsicFunction.create(
                "__string_length",
                (parameters) -> new LongValue(((StringValue) parameters[0]).getValue().length()),
                false,
                Type.STRING
        ));
        addTopLevelFunction("__list_size", IntrinsicFunction.create(
                "__list_size",
                (parameters) -> new LongValue(((ListValue) parameters[0]).getValue().size()),
                false,
                new Type(Type.ANY)
        ));
        addTopLevelFunction("__list_add", IntrinsicFunction.create(
                "__list_add",
                (parameters) -> {
                    ((ListValue) parameters[0]).getValue().add(parameters[1]);
                    return null;
                },
                false,
                new Type(Type.ANY), Type.ANY
        ));
        addTopLevelFunction("__list_remove", IntrinsicFunction.create(
                "__list_remove",
                (parameters) -> {
                    ((ListValue) parameters[0]).getValue().remove((int) ((LongValue) parameters[1]).getValue());
                    return null;
                },
                false,
                new Type(Type.ANY), Type.LONG
        ));
        addTopLevelFunction("__list_get", IntrinsicFunction.create(
                "__list_get",
                (parameters) -> ((ListValue) parameters[0]).getValue().get((int) ((LongValue) parameters[1]).getValue()),
                false,
                new Type(Type.ANY), Type.LONG
        ));
    }
    private final Scope[] scopeStack = new Scope[512];
    {
        this.scopeStack[0] = new Scope(false);
    }
    private int scopeStackIndex;

    public void addTopLevelFunction(String name, IFunction function) {
        this.topLevelFunctions.put(name, function);
    }

    public IValue lookupIdentifier(String name) {
        var index = this.scopeStackIndex;
        while (true) {
            var scope = this.scopeStack[index];
            var local = scope.getLocalVariable(name);
            if (local != null)
                return local;

            index--;
            if (index < 0 || !scope.isSubScope())
                break;
        }

        var global = this.topLevelFunctions.get(name);
        if (global == null)
            return null;

        return new FunctionValue(global);
    }

    public void enterSubScope(ISpan location) {
        enterScope(location, true);
    }

    public void enterScope(ISpan location) {
        enterScope(location, false);
    }

    private void enterScope(ISpan location, boolean subScope) {
        if (this.scopeStackIndex >= this.scopeStack.length - 1)
            throw new InterpreterException(location, "Max scope stack size reached while entering");

        this.scopeStack[++this.scopeStackIndex] = new Scope(subScope);
    }

    public void exitScope() {
        if (this.scopeStackIndex < 1)
            throw new IllegalStateException("Cannot exit the global scope");

        this.scopeStack[this.scopeStackIndex--] = null;
    }

    public Scope currentScope() {
        return this.scopeStack[this.scopeStackIndex];
    }

    public IFunction getTopLevelFunction(String name) {
        var function = this.topLevelFunctions.get(name);
        if (function == null)
            throw new IllegalArgumentException("No top level function with name " + name + " found");

        return function;
    }

    private static final class IntrinsicFunction extends AbstractFunction {

        private final Function<IValue[], IValue> executor;

        private IntrinsicFunction(String name, Function<IValue[], IValue> executor, boolean varargs, Type... parameterTypes) {
            super(name, varargs, parameterTypes);
            this.executor = executor;
        }

        @Override
        public IValue invoke(Interpreter interpreter, IValue... args) {
            return this.executor.apply(args);
        }

        private static IntrinsicFunction create(String name, Function<IValue[], IValue> function, boolean varargs, Type... parameterTypes) {
            return new IntrinsicFunction(name, function, varargs, parameterTypes);
        }
    }
}
