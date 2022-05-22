package com.github.tth05.teth.interpreter.environment;

import com.github.tth05.teth.interpreter.Interpreter;
import com.github.tth05.teth.interpreter.values.*;
import com.github.tth05.teth.lang.parser.Type;

import java.util.*;
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
    }
    private final Deque<Scope> scopeStack = new ArrayDeque<>();
    {
        this.scopeStack.push(new Scope());
    }

    public void addTopLevelFunction(String name, IFunction function) {
        this.topLevelFunctions.put(name, function);
    }

    public IValue lookupIdentifier(String name) {
        var local = currentScope().getLocalVariable(name);
        if (local != null)
            return local;

        var global = this.topLevelFunctions.get(name);
        if (global == null)
            return null;

        return new FunctionValue(global);
    }

    public void enterScope() {
        this.scopeStack.add(new Scope());
    }

    public void exitScope() {
        if (this.scopeStack.size() <= 1)
            throw new IllegalStateException("Cannot exit the global scope");

        this.scopeStack.removeLast();
    }

    public Scope currentScope() {
        return this.scopeStack.getLast();
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
