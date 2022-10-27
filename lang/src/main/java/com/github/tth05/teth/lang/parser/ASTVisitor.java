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

    public void visit(UseStatement useStatement) {
        useStatement.getImports().forEach(i -> i.accept(this));
    }

    public void visit(BlockStatement statement) {
        for (Statement child : statement.getStatements()) {
            if (!this.blockStatementFilter.test(child))
                continue;

            child.accept(this);
        }
    }

    public void visit(ParenthesisedExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visit(BinaryExpression expression) {
        expression.getLeft().accept(this);
        expression.getRight().accept(this);
    }

    public void visit(UnaryExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visit(VariableDeclaration declaration) {
        declaration.getNameExpr().accept(this);
        var typeExpr = declaration.getTypeExpr();
        if (typeExpr != null)
            typeExpr.accept(this);
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
        parameter.getNameExpr().accept(this);
        parameter.getTypeExpr().accept(this);
    }

    public void visit(StructDeclaration declaration) {
        declaration.getNameExpr().accept(this);
        declaration.getGenericParameters().forEach(param -> param.accept(this));
        declaration.getFields().forEach(p -> p.accept(this));
        declaration.getFunctions().forEach(p -> {
            if (!this.blockStatementFilter.test(p))
                return;

            p.accept(this);
        });
    }

    public void visit(StructDeclaration.FieldDeclaration declaration) {
        declaration.getNameExpr().accept(this);
        declaration.getTypeExpr().accept(this);
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
        var advanceStatement = statement.getAdvanceStatement();
        if (advanceStatement != null)
            advanceStatement.accept(this);
        statement.getBody().accept(this);
    }

    public void visit(BreakStatement statement) {
    }

    public void visit(ContinueStatement statement) {
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

    public void visit(NullLiteralExpression doubleLiteralExpression) {
    }

    public void visit(StringLiteralExpression stringLiteralExpression) {
        stringLiteralExpression.getParts().forEach(p -> {
            if (p.getType() == StringLiteralExpression.PartType.EXPRESSION)
                p.asExpression().accept(this);
        });
    }

    public void visit(IdentifierExpression identifierExpression) {
    }

    public void visit(GenericParameterDeclaration declaration) {
        declaration.getNameExpr().accept(this);
    }

    public void visit(TypeExpression typeExpression) {
        typeExpression.getNameExpr().accept(this);
        typeExpression.getGenericParameters().forEach(p -> p.accept(this));
    }

    public void visit(GarbageExpression garbageExpression) {
    }
}
