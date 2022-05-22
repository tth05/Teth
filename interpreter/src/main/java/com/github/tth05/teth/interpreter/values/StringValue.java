package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.InterpreterException;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;

import java.util.Objects;

public class StringValue implements IValue, IBinaryOperatorInvokable {

    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public IValue invokeBinaryOperator(BinaryExpression.Operator operator, IValue arg) {
        if (!(arg instanceof StringValue other))
            throw new InterpreterException("Invalid arguments for intrinsic operation");

        if (operator == BinaryExpression.Operator.OP_ADD) {
            this.value += other.value;
        } else {
            throw new InterpreterException("Unknown intrinsic operation");
        }

        return this;
    }

    @Override
    public IValue copy() {
        return new StringValue(this.value);
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StringValue that = (StringValue) o;

        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return this.value != null ? this.value.hashCode() : 0;
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
