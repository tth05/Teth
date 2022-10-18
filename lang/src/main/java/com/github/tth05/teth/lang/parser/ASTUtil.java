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
            walkNodesAtOffset(statement, offset, collector);
        }

        return collector.statement;
    }

    public static void walkNodesAtOffset(Statement statement, int offset, Consumer<Statement> consumer) {
        if (statement == null)
            return;

        var span = statement.getSpan();
        if (span == null)
            return;

        if (offset < span.offset() || offset >= span.offsetEnd())
            return;

        consumer.accept(statement);

        if (statement instanceof BinaryExpression expr) {
            walkNodesAtOffset(expr.getLeft(), offset, consumer);
            walkNodesAtOffset(expr.getRight(), offset, consumer);
        } else if (statement instanceof BlockStatement block) {
            walkNodesAtOffset(block.getStatements(), offset, consumer);
        } else if (statement instanceof GenericParameterDeclaration p) {
            walkNodesAtOffset(p.getNameExpr(), offset, consumer);
        } else if (statement instanceof FunctionDeclaration func) {
            walkNodesAtOffset(func.getNameExpr(), offset, consumer);
            walkNodesAtOffset(func.getGenericParameters(), offset, consumer);
            walkNodesAtOffset(func.getParameters(), offset, consumer);
            walkNodesAtOffset(func.getReturnTypeExpr(), offset, consumer);
            walkNodesAtOffset(func.getBody(), offset, consumer);
        } else if (statement instanceof FunctionDeclaration.ParameterDeclaration param) {
            walkNodesAtOffset(param.getNameExpr(), offset, consumer);
            walkNodesAtOffset(param.getTypeExpr(), offset, consumer);
        } else if (statement instanceof FunctionInvocationExpression expr) {
            walkNodesAtOffset(expr.getTarget(), offset, consumer);
            walkNodesAtOffset(expr.getGenericParameters(), offset, consumer);
            walkNodesAtOffset(expr.getParameters(), offset, consumer);
        } else if (statement instanceof IfStatement ifStmt) {
            walkNodesAtOffset(ifStmt.getCondition(), offset, consumer);
            walkNodesAtOffset(ifStmt.getBody(), offset, consumer);
            walkNodesAtOffset(ifStmt.getElseStatement(), offset, consumer);
        } else if (statement instanceof ListLiteralExpression expr) {
            walkNodesAtOffset(expr.getInitializers(), offset, consumer);
        } else if (statement instanceof LoopStatement loop) {
            walkNodesAtOffset(loop.getVariableDeclarations(), offset, consumer);
            walkNodesAtOffset(loop.getCondition(), offset, consumer);
            walkNodesAtOffset(loop.getAdvanceStatement(), offset, consumer);
            walkNodesAtOffset(loop.getBody(), offset, consumer);
        } else if (statement instanceof MemberAccessExpression expr) {
            walkNodesAtOffset(expr.getTarget(), offset, consumer);
            walkNodesAtOffset(expr.getMemberNameExpr(), offset, consumer);
        } else if (statement instanceof ObjectCreationExpression expr) {
            walkNodesAtOffset(expr.getTargetNameExpr(), offset, consumer);
            walkNodesAtOffset(expr.getGenericParameters(), offset, consumer);
            walkNodesAtOffset(expr.getParameters(), offset, consumer);
        } else if (statement instanceof ParenthesisedExpression expr) {
            walkNodesAtOffset(expr.getExpression(), offset, consumer);
        } else if (statement instanceof ReturnStatement ret) {
            walkNodesAtOffset(ret.getValueExpr(), offset, consumer);
        } else if (statement instanceof StructDeclaration struct) {
            walkNodesAtOffset(struct.getNameExpr(), offset, consumer);
            walkNodesAtOffset(struct.getGenericParameters(), offset, consumer);
            walkNodesAtOffset(struct.getFields(), offset, consumer);
            walkNodesAtOffset(struct.getFunctions(), offset, consumer);
        } else if (statement instanceof StructDeclaration.FieldDeclaration field) {
            walkNodesAtOffset(field.getNameExpr(), offset, consumer);
            walkNodesAtOffset(field.getTypeExpr(), offset, consumer);
        } else if (statement instanceof TypeExpression expr) {
            walkNodesAtOffset(expr.getNameExpr(), offset, consumer);
            walkNodesAtOffset(expr.getGenericParameters(), offset, consumer);
        } else if (statement instanceof UnaryExpression expr) {
            walkNodesAtOffset(expr.getExpression(), offset, consumer);
        } else if (statement instanceof UseStatement use) {
            walkNodesAtOffset(use.getPathExpr(), offset, consumer);
            walkNodesAtOffset(use.getImports(), offset, consumer);
        } else if (statement instanceof VariableDeclaration var) {
            walkNodesAtOffset(var.getNameExpr(), offset, consumer);
            walkNodesAtOffset(var.getTypeExpr(), offset, consumer);
            walkNodesAtOffset(var.getInitializerExpr(), offset, consumer);
        } else if (statement instanceof StringLiteralExpression expr) {
            for (StringLiteralExpression.Part part : expr.getParts()) {
                if (part.getType() != StringLiteralExpression.PartType.EXPRESSION)
                    continue;

                walkNodesAtOffset(part.asExpression(), offset, consumer);
            }
        }
    }

    private static void walkNodesAtOffset(List<? extends Statement> children, int offset, Consumer<Statement> consumer) {
        for (var child : children)
            walkNodesAtOffset(child, offset, consumer);
    }
}
