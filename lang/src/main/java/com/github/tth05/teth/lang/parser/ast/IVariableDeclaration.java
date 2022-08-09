package com.github.tth05.teth.lang.parser.ast;

public interface IVariableDeclaration {

    IdentifierExpression getNameExpr();

    TypeExpression getTypeExpr();
}
