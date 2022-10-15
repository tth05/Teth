package com.github.tth05.teth.lang.parser.ast;

public sealed interface IHasName permits FunctionDeclaration, GenericParameterDeclaration, IVariableDeclaration, StructDeclaration {

    IdentifierExpression getNameExpr();
}
