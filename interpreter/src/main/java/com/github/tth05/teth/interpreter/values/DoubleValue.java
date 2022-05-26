package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.InterpreterException;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;
import com.github.tth05.teth.lang.parser.ast.UnaryExpression;

public class DoubleValue implements IValue, IBinaryOperatorInvokable, IUnaryOperatorInvokable {

    private double value;

    public DoubleValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public IValue invokeUnaryOperator(UnaryExpression.Operator operator) {
        switch (operator) {
            case OP_NEGATIVE -> this.value = -this.value;
            default -> throw new RuntimeException("Unknown intrinsic operation");
        }

        return this;
    }

    @Override
    public IValue invokeBinaryOperator(BinaryExpression.Operator operator, IValue arg) {
        if (!(arg instanceof DoubleValue other))
            throw new RuntimeException("Invalid arguments for intrinsic operation");

        switch (operator) {
            case OP_POW -> this.value = Math.pow(this.value, other.value);
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
                    default -> throw new RuntimeException("Unknown intrinsic operation");
                };
            }
        }

        return this;
    }

    @Override
    public IValue copy() {
        return new DoubleValue(this.value);
    }

    @Override
    public Type getType() {
        return Type.DOUBLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DoubleValue that = (DoubleValue) o;

        return Double.compare(that.value, this.value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(this.value);
        return (int) (temp ^ (temp >>> 32));
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
