package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.InterpreterException;
import com.github.tth05.teth.interpreter.environment.Environment;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;
import com.github.tth05.teth.lang.parser.ast.UnaryExpression;

public class LongValue implements IValue, IBinaryOperatorInvokable, IUnaryOperatorInvokable, IHasMembers {

    private long value;

    public LongValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public IValue invokeUnaryOperator(UnaryExpression.Operator operator) {
        switch (operator) {
            case OP_NEGATIVE -> this.value = -this.value;
            default -> throw new InterpreterException("Unknown intrinsic operation");
        }

        return this;
    }

    @Override
    public IValue invokeBinaryOperator(BinaryExpression.Operator operator, IValue arg) {
        if (!(arg instanceof LongValue other))
            throw new InterpreterException("Invalid arguments for intrinsic operation");

        switch (operator) {
            case OP_POW -> this.value = (long) Math.pow(this.value, other.value);
            case OP_MULTIPLY -> this.value *= other.value;
            case OP_DIVIDE -> this.value /= other.value;
            case OP_ADD -> this.value += other.value;
            case OP_SUBTRACT -> this.value -= other.value;
            default -> {
                return switch (operator) {
                    case OP_LESS_EQUAL -> new BooleanValue(this.value <= other.value);
                    case OP_GREATER -> new BooleanValue(this.value > other.value);
                    case OP_LESS -> new BooleanValue(this.value < other.value);
                    case OP_GREATER_EQUAL -> new BooleanValue(this.value >= other.value);
                    case OP_EQUAL -> new BooleanValue(this.value == other.value);
                    case OP_NOT_EQUAL -> new BooleanValue(this.value != other.value);
                    default -> throw new InterpreterException("Unknown intrinsic operation");
                };
            }
        }

        return this;
    }

    @Override
    public boolean hasMember(String name) {
        return name.equals("toBinaryString") || name.equals("toString");
    }

    @Override
    public IValue getMember(Environment environment, String name) {
        return switch (name) {
            case "toString" -> new FunctionValue(environment.getTopLevelFunction("__long_toString"));
            case "toBinaryString" -> new FunctionValue(environment.getTopLevelFunction("__long_toBinaryString"));
            default -> throw new IllegalStateException("Unexpected value: " + name);
        };
    }

    @Override
    public IValue copy() {
        return new LongValue(this.value);
    }

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LongValue longValue = (LongValue) o;

        return this.value == longValue.value;
    }

    @Override
    public int hashCode() {
        return (int) (this.value ^ (this.value >>> 32));
    }

    @Override
    public String toString() {
        return getDebugString();
    }

    @Override
    public String getDebugString() {
        return this.value + "";
    }
}
