package com.github.tth05.teth.lang.parser;

import java.util.Objects;

public class Type {

    public static final Type VOID = new Type("void");
    public static final Type ANY = new Type("any");
    public static final Type LONG = new Type("long");
    public static final Type STRING = new Type("string");
    public static final Type DOUBLE = new Type("double");
    public static final Type BOOLEAN = new Type("bool");
    public static final Type FUNCTION = new Type("function");

    private final String name;
    private final Type innerType;

    public Type(String name) {
        this.name = name;
        this.innerType = null;
    }

    public Type(Type innerType) {
        this.name = null;
        this.innerType = innerType;
    }

    public Type getInnerType() {
        return this.innerType;
    }

    public boolean isList() {
        return this.innerType != null;
    }

    public boolean isNumber() {
        return this == LONG || this == DOUBLE;
    }

    public boolean isSubtypeOf(Type other) {
        if (this == other)
            return true;
        if (this == ANY)
            return false;
        if (other == ANY)
            return true;
        if (this.innerType != null ^ other.innerType != null)
            return false;
        if (this.innerType != null)
            return this.innerType.isSubtypeOf(other.innerType);
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Type type = (Type) o;

        return Objects.equals(this.name, type.name) && Objects.equals(this.innerType, type.innerType);
    }

    @Override
    public int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.innerType != null ? this.innerType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (this.innerType != null)
            return this.innerType + "[]";
        return this.name;
    }

    public static Type fromString(String type) {
        return switch (type) {
            case "long" -> Type.LONG;
            case "double" -> Type.DOUBLE;
            case "bool" -> Type.BOOLEAN;
            case "string" -> Type.STRING;
            case "function" -> Type.FUNCTION;
            case "any" -> Type.ANY;
            default -> new Type(type);
        };
    }
}
