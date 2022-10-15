package com.github.tth05.teth.lang.parser.ast;

import com.github.tth05.teth.lang.parser.IDumpable;
import com.github.tth05.teth.lang.parser.IVisitable;
import com.github.tth05.teth.lang.span.Span;

public abstract sealed class Statement implements IDumpable, IVisitable
        permits BlockStatement, Expression, FunctionDeclaration, FunctionDeclaration.ParameterDeclaration,
        GenericParameterDeclaration, IfStatement, LoopStatement, ReturnStatement, StructDeclaration,
        StructDeclaration.FieldDeclaration, TypeExpression, UseStatement, VariableDeclaration {

    private Span span;

    protected Statement(Span span) {
        this.span = span;
    }

    protected void setSpan(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return this.span;
    }
}
