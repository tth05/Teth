package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.environment.Environment;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.BinaryExpression;

import java.util.Objects;

public class StringValue implements IValue, IBinaryOperatorInvokable, IHasMembers {

    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public IValue invokeBinaryOperator(BinaryExpression.Operator operator, IValue arg) {
        if (!(arg instanceof StringValue other))
            throw new RuntimeException("Invalid arguments for intrinsic operation");

        if (operator == BinaryExpression.Operator.OP_ADD) {
            this.value += other.value;
        } else {
            throw new RuntimeException("Unknown intrinsic operation");
        }

        return this;
    }

    @Override
    public boolean hasMember(String name) {
        return name.equals("length");
    }

    @Override
    public IValue getMember(Environment environment, String name) {
        return new FunctionValue(environment.getTopLevelFunction("__string_length"));
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
