package com.github.tth05.teth.bytecodeInterpreter;

public class ObjectValue {

    private final int structId;
    private final Object[] fields;

    public ObjectValue(int structId, Object[] fields) {
        this.structId = structId;
        this.fields = fields;
    }

    public void setField(int fieldIndex, Object value) {
        this.fields[fieldIndex] = value;
    }

    public Object getField(int fieldIndex) {
        return this.fields[fieldIndex];
    }

    public int getStructId() {
        return this.structId;
    }
}