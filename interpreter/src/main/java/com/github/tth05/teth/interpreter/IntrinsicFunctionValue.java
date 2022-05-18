package com.github.tth05.teth.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class IntrinsicFunctionValue implements IValue, IFunction {

    private static final Map<String, IntrinsicFunction> INTRINSIC_FUNCTIONS = new HashMap<>();
    static {
        INTRINSIC_FUNCTIONS.put("print", IntrinsicFunction.create("print", IntrinsicFunction.VAR_ARGS, (parameters) -> {
            for (IValue parameter : parameters)
                System.out.print(parameter.getDebugString() + " ");
            System.out.println();
            return null;
        }));
    }

    private final IntrinsicFunction intrinsicFunction;

    public IntrinsicFunctionValue(String name) {
        var function = INTRINSIC_FUNCTIONS.get(name);
        if (function == null)
            throw new InterpreterException("Unknown intrinsic function: " + name);

        this.intrinsicFunction = function;
    }

    @Override
    public IValue invoke(IValue... args) {
        if (args.length != this.intrinsicFunction.getParameterCount() && this.intrinsicFunction.getParameterCount() != IntrinsicFunction.VAR_ARGS)
            throw new InterpreterException("Invalid number of arguments for intrinsic function: " + this.intrinsicFunction.name);

        return this.intrinsicFunction.invoke(args);
    }

    @Override
    public String getDebugString() {
        return this.intrinsicFunction.name + "()";
    }

    public static boolean isIntrinsicFunction(String name) {
        return INTRINSIC_FUNCTIONS.containsKey(name);
    }

    private static final class IntrinsicFunction implements IFunction {

        public static final int VAR_ARGS = -1;

        private final String name;
        private final int parameterCount;
        private final Function<IValue[], IValue> function;

        private IntrinsicFunction(String name, int parameterCount, Function<IValue[], IValue> function) {
            this.name = name;
            this.parameterCount = parameterCount;
            this.function = function;
        }

        public int getParameterCount() {
            return this.parameterCount;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public IValue invoke(IValue... args) {
            return this.function.apply(args);
        }

        private static IntrinsicFunction create(String name, int parameterCount, Function<IValue[], IValue> function) {
            return new IntrinsicFunction(name, parameterCount, function);
        }
    }
}
