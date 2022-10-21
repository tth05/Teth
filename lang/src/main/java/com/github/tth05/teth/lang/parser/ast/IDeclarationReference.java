package com.github.tth05.teth.lang.parser.ast;

/**
 * Something which references a declaration of any type. For example any identifier or member access expressions.
 */
public sealed interface IDeclarationReference permits BooleanLiteralExpression, BreakStatement, ContinueStatement, DoubleLiteralExpression, IAssignmentTarget, ListLiteralExpression, LongLiteralExpression, ObjectCreationExpression, StringLiteralExpression, TypeExpression {
}
