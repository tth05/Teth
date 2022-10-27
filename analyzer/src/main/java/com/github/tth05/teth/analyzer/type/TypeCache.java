package com.github.tth05.teth.analyzer.type;

import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.lang.parser.ast.GenericParameterDeclaration;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.parser.ast.StructDeclaration;

import java.util.*;
import java.util.stream.Collectors;

public class TypeCache {

    private static final SemanticType VOID = new SemanticType(0);

    /**
     * 0 is reserved for VOID
     */
    private int id = 1;

    private final Map<Statement, Integer> statementToTypeIdMap = new IdentityHashMap<>();
    private final Map<Integer, Statement> typeIdToStatementMap = new IdentityHashMap<>();
    private final Map<Integer, SemanticType> rawTypeCache = new HashMap<>();

    public int internalizeType(Statement statement) {
        if (!(statement instanceof GenericParameterDeclaration) && !(statement instanceof StructDeclaration))
            throw new IllegalArgumentException(statement + "");

        return this.statementToTypeIdMap.computeIfAbsent(statement, k -> {
            var id = this.id++;
            this.typeIdToStatementMap.put(id, statement);
            return id;
        });
    }

    public SemanticType getType(Statement statement) {
        return this.rawTypeCache.computeIfAbsent(this.internalizeType(statement), SemanticType::new);
    }

    public Statement getDeclaration(SemanticType type) {
        return this.typeIdToStatementMap.get(type.getTypeId());
    }

    public boolean isNumber(SemanticType type) {
        return type == getType(Prelude.LONG_STRUCT_DECLARATION) || type == getType(Prelude.DOUBLE_STRUCT_DECLARATION);
    }

    public SemanticType voidType() {
        return VOID;
    }

    public boolean isSubtypeOf(SemanticType subType, SemanticType type) {
        if (subType == null || type == null)
            return false;
        if (subType == VOID)
            return type == VOID;
        if (type == SemanticType.NULL)
            return false;
        if (subType == SemanticType.NULL)
            return type != getType(Prelude.LONG_STRUCT_DECLARATION) && type != getType(Prelude.DOUBLE_STRUCT_DECLARATION) && type != getType(Prelude.BOOLEAN_STRUCT_DECLARATION);

        var ANY = getType(Prelude.ANY_STRUCT_DECLARATION);
        if (subType == type)
            return true;
        if (type == ANY) // long, double etc. are subtypes of any
            return true;
        if (subType == ANY) // any is not a subtype of anything, except for any
            return false;
        if (!Objects.equals(subType.getTypeId(), type.getTypeId())) // Different types
            return false;
        if (subType.hasGenericBounds() ^ type.hasGenericBounds()) // One has generic bounds, the other doesn't
            return false;
        if (subType.hasGenericBounds()) { // Different generic bounds
            for (int i = 0; i < subType.getGenericBounds().size(); i++) {
                if (!isSubtypeOf(subType.getGenericBounds().get(i), type.getGenericBounds().get(i)))
                    return false;
            }
        }
        return true;
    }

    public String toString(SemanticType type) {
        if (type == null || type == SemanticType.UNRESOLVED)
            return "???";
        if (type == VOID)
            return "void";
        if (type == SemanticType.NULL)
            return "null";

        String name;
        // TODO: Switch preview disabled
        var declaration = getDeclaration(type);
        if (declaration instanceof StructDeclaration struct) {
            name = struct.getNameExpr().getValue();
        } else if (declaration instanceof GenericParameterDeclaration generic) {
            name = generic.getNameExpr().getValue();
        } else {
            throw new IllegalStateException();
        }

        if (type.hasGenericBounds())
            return name + "<" + type.getGenericBounds().stream()
                    .map(this::toString)
                    .collect(Collectors.joining(", ")) + ">";
        return name;
    }
}
