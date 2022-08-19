package com.github.tth05.teth.analyzer.type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SemanticType {

    /*public static final Type VOID = new Type("void");
    public static final Type ANY = new Type("any");
    public static final Type LONG = new Type("long");
    public static final Type STRING = new Type("string");
    public static final Type DOUBLE = new Type("double");
    public static final Type BOOLEAN = new Type("bool");
    public static final Type FUNCTION = new Type("function");*/

    private final int typeId;
    private final List<SemanticType> genericBounds;

    SemanticType(int typeId) {
        this(typeId, null);
    }

    public SemanticType(int typeId, List<SemanticType> genericBounds) {
        this.typeId = typeId;
        this.genericBounds = genericBounds;
    }

    public int getTypeId() {
        return this.typeId;
    }

    public boolean hasGenericBounds() {
        return this.genericBounds != null;
    }

    public List<SemanticType> getGenericBounds() {
        return Collections.unmodifiableList(this.genericBounds);
    }

/*    public boolean isSubtypeOf(Type other) {
        if (this == VOID)
            return other == VOID;

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
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SemanticType type = (SemanticType) o;

        if (this.typeId != type.typeId)
            return false;
        return Objects.equals(this.genericBounds, type.genericBounds);
    }

    @Override
    public int hashCode() {
        int result = this.typeId;
        result = 31 * result + (this.genericBounds != null ? this.genericBounds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Wrong");
    }

/*    public static Type list(Type type) {
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
    }*/
}
