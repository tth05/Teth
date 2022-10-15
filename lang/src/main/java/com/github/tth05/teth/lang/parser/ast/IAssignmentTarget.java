package com.github.tth05.teth.lang.parser.ast;

public sealed interface IAssignmentTarget extends IDeclarationReference permits IdentifierExpression, MemberAccessExpression {
}
