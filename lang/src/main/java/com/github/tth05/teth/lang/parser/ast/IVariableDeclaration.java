package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.Type;

public interface IVariableDeclaration {

    IdentifierExpression getNameExpr();

    TypeExpression getTypeExpr();

    /**
     * Hints this declaration that its new type should be the given {@code type}. Internally, a type expression should
     * be created such that {@link #getTypeExpr()} conforms to this new inferred type.
     *
     * @param type The new type
     */
    void setInferredType(Type type);
}
