package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.*;

import java.util.function.Predicate;

public abstract class ASTVisitor {

    private Predicate<Statement> blockStatementFilter = s -> true;

    public void setBlockStatementFilter(Predicate<Statement> blockStatementFilter) {
        this.blockStatementFilter = blockStatementFilter;
    }

    public void visit(SourceFileUnit unit) {
        for (Statement statement : unit.getStatements()) {
            if (!this.blockStatementFilter.test(statement))
                continue;

            statement.accept(this);
        }
    }

    public void visit(BlockStatement statement) {
        for (Statement child : statement.getStatements()) {
            if (!this.blockStatementFilter.test(statement))
                continue;

            child.accept(this);
        }
    }

    public void visit(BinaryExpression expression) {
        expression.getLeft().accept(this);
        expression.getRight().accept(this);
    }

    public void visit(UnaryExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visit(VariableAssignmentExpression expression) {
        expression.getTargetExpr().accept(this);
        expression.getExpr().accept(this);
    }

    public void visit(VariableDeclaration declaration) {
        var typeExpr = declaration.getTypeExpr();
        if (typeExpr != null)
            typeExpr.accept(this);
        declaration.getNameExpr().accept(this);
        declaration.getInitializerExpr().accept(this);
    }

    public void visit(FunctionDeclaration declaration) {
        declaration.getNameExpr().accept(this);
        declaration.getGenericParameters().forEach(param -> param.accept(this));
        declaration.getParameters().forEach(p -> p.accept(this));
        var expr = declaration.getReturnTypeExpr();
        if (expr != null)
            expr.accept(this);
        declaration.getBody().accept(this);
    }

    public void visit(FunctionDeclaration.ParameterDeclaration parameter) {
        parameter.getTypeExpr().accept(this);
        parameter.getNameExpr().accept(this);
    }

    public void visit(StructDeclaration declaration) {
        declaration.getNameExpr().accept(this);
        declaration.getFields().forEach(p -> p.accept(this));
        declaration.getFunctions().forEach(p -> {
            if (!this.blockStatementFilter.test(p))
                return;

            p.accept(this);
        });
    }

    public void visit(StructDeclaration.FieldDeclaration declaration) {
        declaration.getTypeExpr().accept(this);
        declaration.getNameExpr().accept(this);
    }

    public void visit(ObjectCreationExpression expression) {
        expression.getTargetNameExpr().accept(this);
        expression.getGenericParameters().forEach(param -> param.accept(this));
        expression.getParameters().forEach(p -> p.accept(this));
    }

    public void visit(FunctionInvocationExpression invocation) {
        invocation.getTarget().accept(this);
        invocation.getGenericParameters().forEach(p -> p.accept(this));
        invocation.getParameters().forEach(e -> e.accept(this));
    }

    public void visit(IfStatement statement) {
        statement.getCondition().accept(this);
        statement.getBody().accept(this);
        var elseStatement = statement.getElseStatement();
        if (elseStatement != null)
            elseStatement.accept(this);
    }

    public void visit(LoopStatement statement) {
        statement.getVariableDeclarations().forEach(v -> v.accept(this));
        var condition = statement.getCondition();
        if (condition != null)
            condition.accept(this);
        statement.getBody().accept(this);
        var advanceStatement = statement.getAdvanceStatement();
        if (advanceStatement != null)
            advanceStatement.accept(this);
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

    public void visit(GenericParameterDeclaration declaration) {
    }

    public void visit(TypeExpression typeExpression) {
    }
}
