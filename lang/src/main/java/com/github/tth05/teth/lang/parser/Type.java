package com.github.tth05.teth.lang.parser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Type {

    public static final Type VOID = new Type("void");
    public static final Type ANY = new Type("any");
    public static final Type LONG = new Type("long");
    public static final Type STRING = new Type("string");
    public static final Type DOUBLE = new Type("double");
    public static final Type BOOLEAN = new Type("bool");
    public static final Type FUNCTION = new Type("function");

    private final String name;
    private final List<Type> genericBounds;

    public Type(String name) {
        this(name, null);
    }

    public Type(String name, List<Type> genericBounds) {
        this.name = name;
        this.genericBounds = genericBounds;
    }

    public String getName() {
        return this.name;
    }

    public boolean isList() {
        return this.name.equals("list");
    }

    public boolean isNumber() {
        return this == LONG || this == DOUBLE;
    }

    public boolean hasGenericBounds() {
        return this.genericBounds != null;
    }

    public List<Type> getGenericBounds() {
        return Collections.unmodifiableList(this.genericBounds);
    }

    public boolean isSubtypeOf(Type other) {
        if (this == VOID)
            return false;

        if (this == other)
            return true;
        if (other == ANY) // long, double etc. are subtypes of any
            return true;
        if (this == ANY) // any is not a subtype of anything, except for any
            return false;
        if (!Objects.equals(this.name, other.name)) // Different types
            return false;
        if (hasGenericBounds() ^ other.hasGenericBounds()) // One has generic bounds, the other doesn't
            return false;
        if (hasGenericBounds()) { // Different generic bounds
            for (int i = 0; i < this.genericBounds.size(); i++) {
                if (!this.genericBounds.get(i).isSubtypeOf(other.genericBounds.get(i)))
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Type type = (Type) o;

        if (!this.name.equals(type.name))
            return false;
        return Objects.equals(this.genericBounds, type.genericBounds);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + (this.genericBounds != null ? this.genericBounds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.name + (!hasGenericBounds() ? "" : "<" + this.genericBounds.stream().map(Type::toString).collect(Collectors.joining(", ")) + ">");
    }

    public static Type list(Type type) {
        return new Type("list", List.of(type));
    }

    public static Type fromName(String name) {
        return switch (name) {
            case "long" -> Type.LONG;
            case "double" -> Type.DOUBLE;
            case "bool" -> Type.BOOLEAN;
            case "string" -> Type.STRING;
            case "function" -> Type.FUNCTION;
            case "any" -> Type.ANY;
            default -> new Type(name);
        };
    }

    public static Type fromNameWithGenericBounds(String type, List<Type> genericBounds) {
        return new Type(type, genericBounds);
    }
}
