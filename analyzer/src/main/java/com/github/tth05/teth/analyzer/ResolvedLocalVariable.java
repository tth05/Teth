package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.IdentifierExpression;
import com.github.tth05.teth.lang.parser.ast.TypeExpression;

public class ResolvedLocalVariable {

    private final IdentifierExpression nameExpr;
    private final TypeExpression typeExpr;

    public ResolvedLocalVariable(IdentifierExpression nameExpr, TypeExpression typeExpr) {
        this.nameExpr = nameExpr;
        this.typeExpr = typeExpr;
    }
}
