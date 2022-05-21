package com.github.tth05.teth.lang.parser;

public class Type {

    public static final Type ANY = new Type("any");
    public static final Type LONG = new Type("long");
    public static final Type DOUBLE = new Type("double");
    public static final Type BOOLEAN = new Type("bool");
    public static final Type FUNCTION = new Type("function");

    private final String name;

    public Type(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Type type))
            return false;
        if (this == ANY || o == ANY)
            return true;

        return this.name.equals(type.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static Type fromString(String type) {
        return switch (type) {
            case "long" -> Type.LONG;
            case "double" -> Type.DOUBLE;
            case "bool" -> Type.BOOLEAN;
            case "function" -> Type.FUNCTION;
            default -> new Type(type);
        };
    }
}
