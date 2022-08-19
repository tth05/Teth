package com.github.tth05.teth.analyzer.type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SemanticType {

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
}
