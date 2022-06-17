package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IdentifierExpression;
import com.github.tth05.teth.lang.parser.ast.TypeExpression;

public class ResolvedSourceFunction {

    private final IdentifierExpression nameExpr;
    private final FunctionDeclaration.Parameter parameters;
    private final TypeExpression returnTypeExpr;

    private final DeclarationStack scope = new DeclarationStack();

    public ResolvedSourceFunction(IdentifierExpression nameExpr, FunctionDeclaration.Parameter parameters, TypeExpression returnTypeExpr) {
        this.nameExpr = nameExpr;
        this.parameters = parameters;
        this.returnTypeExpr = returnTypeExpr;
    }

    public DeclarationStack getScope() {
        return this.scope;
    }
}
