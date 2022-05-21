package com.github.tth05.teth.interpreter.environment;

import com.github.tth05.teth.interpreter.Interpreter;
import com.github.tth05.teth.interpreter.values.AbstractFunction;
import com.github.tth05.teth.interpreter.values.IFunction;
import com.github.tth05.teth.interpreter.values.IValue;
import com.github.tth05.teth.lang.parser.Type;

import java.util.*;
import java.util.function.Function;

public class Environment {

    private final Map<String, List<IFunction>> topLevelFunctions = new HashMap<>();
    {
        addTopLevelFunction("print", IntrinsicFunction.create("print", (parameters) -> {
            for (IValue parameter : parameters)
                System.out.print(parameter.getDebugString() + " ");
            System.out.println();
            return null;
        }, true, Type.ANY));
    }
    private final Deque<Scope> scopeStack = new ArrayDeque<>();
    {
        this.scopeStack.push(new Scope());
    }

    public void addTopLevelFunction(String name, IFunction function) {
        this.topLevelFunctions.computeIfAbsent(name, (key) -> new ArrayList<>()).add(function);
    }

    public IFunction lookupFunction(String name, IValue[] parameters) {
        var local = currentScope().getLocalVariable(name);
        if (local instanceof IFunction f && f.isApplicable(parameters))
            return f;

        var global = this.topLevelFunctions.get(name);
        if (global == null)
            return null;

        for (var f : global) {
            if (f.isApplicable(parameters))
                return f;
        }

        return null;
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
