package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.environment.Environment;
import com.github.tth05.teth.lang.parser.Type;

import java.util.List;

public class ListValue implements IValue, IHasMembers {

    private final List<IValue> value;
    private final Type type;

    public ListValue(Type innerType, List<IValue> value) {
        this.type = new Type(innerType);
        this.value = value;
    }

    public List<IValue> getValue() {
        return this.value;
    }

    @Override
    public boolean hasMember(String name) {
        return name.equals("size") || name.equals("add") || name.equals("remove") || name.equals("get");
    }

    @Override
    public IValue getMember(Environment environment, String name) {
        return switch (name) {
            case "size" -> new FunctionValue(environment.getTopLevelFunction("__list_size"));
            case "add" -> new FunctionValue(environment.getTopLevelFunction("__list_add"));
            case "remove" -> new FunctionValue(environment.getTopLevelFunction("__list_remove"));
            case "get" -> new FunctionValue(environment.getTopLevelFunction("__list_get"));
            default -> throw new IllegalStateException("Unexpected value: " + name);
        };
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ListValue listValue = (ListValue) o;

        if (!this.value.equals(listValue.value))
            return false;
        return this.type.equals(listValue.type);
    }

    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
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
