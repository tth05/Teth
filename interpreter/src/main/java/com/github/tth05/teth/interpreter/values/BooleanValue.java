package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.InterpreterException;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;
import com.github.tth05.teth.lang.parser.ast.UnaryExpression;

public class BooleanValue implements IValue, IBinaryOperatorInvokable, IUnaryOperatorInvokable {

    private boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public IValue invokeUnaryOperator(UnaryExpression.Operator operator) {
        switch (operator) {
            case OP_NOT -> this.value = !this.value;
            default -> throw new InterpreterException("Unknown intrinsic operation");
        }

        return this;
    }

    @Override
    public IValue invokeBinaryOperator(BinaryExpression.Operator operator, IValue arg) {
        if (!(arg instanceof BooleanValue other))
            throw new InterpreterException("Invalid arguments for intrinsic operation");

        return switch (operator) {
            case OP_EQUAL -> new BooleanValue(this.value == other.value);
            case OP_NOT_EQUAL -> new BooleanValue(this.value != other.value);
            default -> throw new InterpreterException("Unknown intrinsic operation");
        };
    }

    @Override
    public IValue copy() {
        return new BooleanValue(this.value);
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BooleanValue that = (BooleanValue) o;

        return this.value == that.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(this.value);
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
