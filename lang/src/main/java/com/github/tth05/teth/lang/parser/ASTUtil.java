package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;

import java.util.List;

public class ASTUtil {

    /**
     * @return Returns the deepest node in the AST with a non-null {@link Statement#getSpan()} where
     * {@link Span#offset()} is equal to {@code offset}.
     */
    public static Statement findStatementAtExact(SourceFileUnit unit, int offset) {
        return findStatementAtExact(unit.getStatements(), offset);
    }

    public static Statement findStatementAtExact(Statement statement, int offset) {
        if (statement == null)
            return null;

        var span = statement.getSpan();
        if (span == null)
            return null;

        if (offset < span.offset() || offset >= span.offsetEnd())
            return null;

        if (statement instanceof BinaryExpression expr) {
            var left = findStatementAtExact(expr.getLeft(), offset);
            if (left != null)
                return left;

            var right = findStatementAtExact(expr.getRight(), offset);
            if (right != null)
                return right;
        } else if (statement instanceof BlockStatement block) {
            statement = findStatementAtExact(block.getStatements(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof GenericParameterDeclaration p) {
            statement = findStatementAtExact(p.getNameExpr(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof FunctionDeclaration func) {
            statement = findStatementAtExact(func.getNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(func.getGenericParameters(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(func.getParameters(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(func.getReturnTypeExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(func.getBody(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof FunctionDeclaration.ParameterDeclaration param) {
            statement = findStatementAtExact(param.getNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(param.getTypeExpr(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof FunctionInvocationExpression expr) {
            statement = findStatementAtExact(expr.getTarget(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(expr.getGenericParameters(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(expr.getParameters(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof IfStatement ifStmt) {
            statement = findStatementAtExact(ifStmt.getCondition(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(ifStmt.getBody(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(ifStmt.getElseStatement(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof ListLiteralExpression expr) {
            statement = findStatementAtExact(expr.getInitializers(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof LoopStatement loop) {
            statement = findStatementAtExact(loop.getVariableDeclarations(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(loop.getCondition(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(loop.getAdvanceStatement(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(loop.getBody(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof MemberAccessExpression expr) {
            statement = findStatementAtExact(expr.getTarget(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(expr.getMemberNameExpr(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof ObjectCreationExpression expr) {
            statement = findStatementAtExact(expr.getTargetNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(expr.getGenericParameters(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(expr.getParameters(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof ParenthesisedExpression expr) {
            statement = findStatementAtExact(expr.getExpression(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof ReturnStatement ret) {
            statement = findStatementAtExact(ret.getValueExpr(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof StructDeclaration struct) {
            statement = findStatementAtExact(struct.getNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(struct.getGenericParameters(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(struct.getFields(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(struct.getFunctions(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof StructDeclaration.FieldDeclaration field) {
            statement = findStatementAtExact(field.getNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(field.getTypeExpr(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof TypeExpression expr) {
            statement = findStatementAtExact(expr.getNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(expr.getGenericParameters(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof UnaryExpression expr) {
            statement = findStatementAtExact(expr.getExpression(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof UseStatement use) {
            statement = findStatementAtExact(use.getPathExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(use.getImports(), offset);
            if (statement != null)
                return statement;
        } else if (statement instanceof VariableDeclaration var) {
            statement = findStatementAtExact(var.getNameExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(var.getTypeExpr(), offset);
            if (statement != null)
                return statement;

            statement = findStatementAtExact(var.getInitializerExpr(), offset);
            if (statement != null)
                return statement;
        }

        return statement;
    }

    private static Statement findStatementAtExact(List<? extends Statement> children, int offset) {
        for (var child : children) {
            var result = findStatementAtExact(child, offset);
            if (result != null)
                return result;
        }

        return null;
    }
}
