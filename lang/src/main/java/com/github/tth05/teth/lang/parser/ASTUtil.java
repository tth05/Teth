package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;

import java.util.List;
import java.util.function.Consumer;

public class ASTUtil {

    /**
     * @return Returns the deepest node in the AST with a non-null {@link Statement#getSpan()} where
     * {@link Span#offset()} is equal to {@code offset}.
     */
    public static Statement findStatementAtExact(SourceFileUnit unit, int offset) {
        class NodeCollector implements Consumer<Statement> {

            Statement statement = null;

            @Override
            public void accept(Statement statement) {
                if (statement.getSpan().offset() == offset)
                    this.statement = statement;
            }
        }

        var collector = new NodeCollector();

        for (var statement : unit.getStatements()) {
            walkNodesAtOffset(statement, offset, false, collector);
        }

        return collector.statement;
    }

    public static void walkNodesAtOffset(Statement statement, int offset, boolean endInclusive, Consumer<Statement> consumer) {
        if (statement == null)
            return;

        var span = statement.getSpan();
        if (span == null)
            return;

        if (offset < span.offset() || offset > span.offsetEnd() || (!endInclusive && offset == span.offsetEnd()))
            return;

        consumer.accept(statement);

        if (statement instanceof BinaryExpression expr) {
            walkNodesAtOffset(expr.getLeft(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getRight(), offset, endInclusive, consumer);
        } else if (statement instanceof BlockStatement block) {
            walkNodesAtOffset(block.getStatements(), offset, endInclusive, consumer);
        } else if (statement instanceof GenericParameterDeclaration p) {
            walkNodesAtOffset(p.getNameExpr(), offset, endInclusive, consumer);
        } else if (statement instanceof FunctionDeclaration func) {
            walkNodesAtOffset(func.getNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(func.getGenericParameters(), offset, endInclusive, consumer);
            walkNodesAtOffset(func.getParameters(), offset, endInclusive, consumer);
            walkNodesAtOffset(func.getReturnTypeExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(func.getBody(), offset, endInclusive, consumer);
        } else if (statement instanceof FunctionDeclaration.ParameterDeclaration param) {
            walkNodesAtOffset(param.getNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(param.getTypeExpr(), offset, endInclusive, consumer);
        } else if (statement instanceof FunctionInvocationExpression expr) {
            walkNodesAtOffset(expr.getTarget(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getGenericParameters(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getParameters(), offset, endInclusive, consumer);
        } else if (statement instanceof IfStatement ifStmt) {
            walkNodesAtOffset(ifStmt.getCondition(), offset, endInclusive, consumer);
            walkNodesAtOffset(ifStmt.getBody(), offset, endInclusive, consumer);
            walkNodesAtOffset(ifStmt.getElseStatement(), offset, endInclusive, consumer);
        } else if (statement instanceof ListLiteralExpression expr) {
            walkNodesAtOffset(expr.getInitializers(), offset, endInclusive, consumer);
        } else if (statement instanceof LoopStatement loop) {
            walkNodesAtOffset(loop.getVariableDeclarations(), offset, endInclusive, consumer);
            walkNodesAtOffset(loop.getCondition(), offset, endInclusive, consumer);
            walkNodesAtOffset(loop.getAdvanceStatement(), offset, endInclusive, consumer);
            walkNodesAtOffset(loop.getBody(), offset, endInclusive, consumer);
        } else if (statement instanceof MemberAccessExpression expr) {
            walkNodesAtOffset(expr.getTarget(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getMemberNameExpr(), offset, endInclusive, consumer);
        } else if (statement instanceof ObjectCreationExpression expr) {
            walkNodesAtOffset(expr.getTargetNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getGenericParameters(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getParameters(), offset, endInclusive, consumer);
        } else if (statement instanceof ParenthesisedExpression expr) {
            walkNodesAtOffset(expr.getExpression(), offset, endInclusive, consumer);
        } else if (statement instanceof ReturnStatement ret) {
            walkNodesAtOffset(ret.getValueExpr(), offset, endInclusive, consumer);
        } else if (statement instanceof StructDeclaration struct) {
            walkNodesAtOffset(struct.getNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(struct.getGenericParameters(), offset, endInclusive, consumer);
            walkNodesAtOffset(struct.getFields(), offset, endInclusive, consumer);
            walkNodesAtOffset(struct.getFunctions(), offset, endInclusive, consumer);
        } else if (statement instanceof StructDeclaration.FieldDeclaration field) {
            walkNodesAtOffset(field.getNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(field.getTypeExpr(), offset, endInclusive, consumer);
        } else if (statement instanceof TypeExpression expr) {
            walkNodesAtOffset(expr.getNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(expr.getGenericParameters(), offset, endInclusive, consumer);
        } else if (statement instanceof UnaryExpression expr) {
            walkNodesAtOffset(expr.getExpression(), offset, endInclusive, consumer);
        } else if (statement instanceof UseStatement use) {
            walkNodesAtOffset(use.getPathExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(use.getImports(), offset, endInclusive, consumer);
        } else if (statement instanceof VariableDeclaration var) {
            walkNodesAtOffset(var.getNameExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(var.getTypeExpr(), offset, endInclusive, consumer);
            walkNodesAtOffset(var.getInitializerExpr(), offset, endInclusive, consumer);
        } else if (statement instanceof StringLiteralExpression expr) {
            for (StringLiteralExpression.Part part : expr.getParts()) {
                if (part.getType() != StringLiteralExpression.PartType.EXPRESSION)
                    continue;

                walkNodesAtOffset(part.asExpression(), offset, endInclusive, consumer);
            }
        }
    }

    public static void walkNodesAtOffset(List<? extends Statement> children, int offset, boolean endInclusive, Consumer<Statement> consumer) {
        for (var child : children)
            walkNodesAtOffset(child, offset, endInclusive, consumer);
    }
}
