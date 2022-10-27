package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.span.Span;

public abstract sealed class Expression extends Statement permits BinaryExpression, BooleanLiteralExpression, DoubleLiteralExpression, FunctionInvocationExpression, GarbageExpression, IdentifierExpression, ListLiteralExpression, LongLiteralExpression, MemberAccessExpression, NullLiteralExpression, ObjectCreationExpression, ParenthesisedExpression, StringLiteralExpression, UnaryExpression {

    protected Expression(Span span) {
        super(span);
    }
}
