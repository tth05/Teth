package com.github.tth05.teth.interpreter;

public class BooleanValue implements IValue {

    private boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
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
    public String getDebugString() {
        return this.value + "";
    }
}
