package com.github.tth05.teth.bytecodeInterpreter;

public class ObjectValue {

    private final Object[] fields;

    public ObjectValue(Object[] fields) {
        this.fields = fields;
    }

    public void setField(int fieldIndex, Object value) {
        this.fields[fieldIndex] = value;
    }

    public Object getField(int fieldIndex) {
        return this.fields[fieldIndex];
    }

    public Object[] getFields() {
        return this.fields;
    }
}
