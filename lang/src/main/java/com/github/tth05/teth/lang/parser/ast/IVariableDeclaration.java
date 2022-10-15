package com.github.tth05.teth.lang.parser.ast;

public sealed interface IVariableDeclaration extends IHasName permits FunctionDeclaration.ParameterDeclaration, StructDeclaration.FieldDeclaration, VariableDeclaration {

    TypeExpression getTypeExpr();
}
