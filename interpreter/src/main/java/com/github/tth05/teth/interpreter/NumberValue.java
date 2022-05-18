package com.github.tth05.teth.interpreter;

import com.github.tth05.teth.lang.ast.BinaryExpression;

public class NumberValue implements IValue, IBinaryOperatorInvokable {

    private double value;

    public NumberValue(double value) {
        this.value = value;
    }

    @Override
    public IValue invokeOperator(BinaryExpression.Operator operator, IValue arg) {
        if (!(arg instanceof NumberValue other))
            throw new InterpreterException("Invalid arguments for intrinsic operation");

        switch (operator) {
            case OP_ADD -> this.value += other.value;
            case OP_SUBTRACT -> this.value -= other.value;
            case OP_MULTIPLY -> this.value *= other.value;
            case OP_DIVIDE -> this.value /= other.value;
            default -> throw new InterpreterException("Unknown intrinsic operation");
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NumberValue that = (NumberValue) o;

        return Double.compare(that.value, this.value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(this.value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String getDebugString() {
        return this.value + "";
    }
}
