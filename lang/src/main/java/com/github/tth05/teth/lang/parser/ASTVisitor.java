package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.*;

public abstract class ASTVisitor {

    public void visit(SourceFileUnit unit) {
        for (Statement statement : unit.getStatements()) {
            statement.accept(this);
        }
    }

    public void visit(BinaryExpression expression) {
        expression.getLeft().accept(this);
        expression.getRight().accept(this);
    }

    public void visit(UnaryExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visit(BlockStatement statement) {
        for (Statement child : statement.getStatements()) {
            child.accept(this);
        }
    }

    public void visit(VariableAssignmentExpression expression) {
        expression.getTargetNameExpr().accept(this);
        expression.getExpr().accept(this);
    }

    public void visit(VariableDeclaration declaration) {
        declaration.getTypeExpr().accept(this);
        declaration.getNameExpr().accept(this);
        var expression = declaration.getExpression();
        if (expression != null)
            expression.accept(this);
    }

    public void visit(FunctionDeclaration declaration) {
        declaration.getNameExpr().accept(this);
        declaration.getParameters().forEach(p -> {
            p.type().accept(this);
            p.name().accept(this);
        });
        declaration.getReturnTypeExpr().accept(this);
        declaration.getBody().accept(this);
    }

    public void visit(FunctionInvocationExpression invocation) {
        invocation.getTarget().accept(this);
        invocation.getParameters().forEach(e -> e.accept(this));
    }

    public void visit(IfStatement statement) {
        statement.getCondition().accept(this);
        statement.getBody().accept(this);
        var elseStatement = statement.getElseStatement();
        if (elseStatement != null)
            elseStatement.accept(this);
    }

    public void visit(ListLiteralExpression listLiteralExpression) {
        listLiteralExpression.getInitializers().forEach(e -> e.accept(this));
    }

    public void visit(MemberAccessExpression expression) {
        expression.getTarget().accept(this);
        expression.getMemberNameExpr().accept(this);
    }

    public void visit(ReturnStatement returnStatement) {
        var valueExpr = returnStatement.getValueExpr();
        if (valueExpr != null)
            valueExpr.accept(this);
    }

    public void visit(BooleanLiteralExpression booleanLiteralExpression) {
    }

    public void visit(LongLiteralExpression longLiteralExpression) {
    }

    public void visit(DoubleLiteralExpression doubleLiteralExpression) {
    }

    public void visit(StringLiteralExpression stringLiteralExpression) {
    }

    public void visit(IdentifierExpression identifierExpression) {
    }

    public void visit(TypeExpression typeExpression) {
    }
}
