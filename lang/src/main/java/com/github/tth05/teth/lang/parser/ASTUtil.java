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
            var child = findStatementAtExact(block.getStatements(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof GenericParameterDeclaration p) {
            var child = findStatementAtExact(p.getNameExpr(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof FunctionDeclaration func) {
            var child = findStatementAtExact(func.getNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(func.getGenericParameters(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(func.getParameters(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(func.getReturnTypeExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(func.getBody(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof FunctionDeclaration.ParameterDeclaration param) {
            var child = findStatementAtExact(param.getNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(param.getTypeExpr(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof FunctionInvocationExpression expr) {
            var child = findStatementAtExact(expr.getTarget(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(expr.getGenericParameters(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(expr.getParameters(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof IfStatement ifStmt) {
            var child = findStatementAtExact(ifStmt.getCondition(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(ifStmt.getBody(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(ifStmt.getElseStatement(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof ListLiteralExpression expr) {
            var child = findStatementAtExact(expr.getInitializers(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof LoopStatement loop) {
            var child = findStatementAtExact(loop.getVariableDeclarations(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(loop.getCondition(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(loop.getAdvanceStatement(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(loop.getBody(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof MemberAccessExpression expr) {
            var child = findStatementAtExact(expr.getTarget(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(expr.getMemberNameExpr(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof ObjectCreationExpression expr) {
            var child = findStatementAtExact(expr.getTargetNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(expr.getGenericParameters(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(expr.getParameters(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof ParenthesisedExpression expr) {
            var child = findStatementAtExact(expr.getExpression(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof ReturnStatement ret) {
            var child = findStatementAtExact(ret.getValueExpr(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof StructDeclaration struct) {
            var child = findStatementAtExact(struct.getNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(struct.getGenericParameters(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(struct.getFields(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(struct.getFunctions(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof StructDeclaration.FieldDeclaration field) {
            var child = findStatementAtExact(field.getNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(field.getTypeExpr(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof TypeExpression expr) {
            var child = findStatementAtExact(expr.getNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(expr.getGenericParameters(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof UnaryExpression expr) {
            var child = findStatementAtExact(expr.getExpression(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof UseStatement use) {
            var child = findStatementAtExact(use.getPathExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(use.getImports(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof VariableDeclaration var) {
            var child = findStatementAtExact(var.getNameExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(var.getTypeExpr(), offset);
            if (child != null)
                return child;

            child = findStatementAtExact(var.getInitializerExpr(), offset);
            if (child != null)
                return child;
        } else if (statement instanceof StringLiteralExpression expr) {
            for (StringLiteralExpression.Part part : expr.getParts()) {
                if (part.getType() != StringLiteralExpression.PartType.EXPRESSION)
                    continue;

                var child = findStatementAtExact(part.asExpression(), offset);
                if (child != null)
                    return child;
            }
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
