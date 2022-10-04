package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.lang.parser.ast.*;
import com.github.tth05.teth.lang.span.Span;

import java.util.Optional;

public class ReturnStatementVerifier extends AnalysisASTVisitor {

    public ReturnStatementVerifier() {
        // Skips imports, top level code etc.
        setBlockStatementFilter(s -> s instanceof StructDeclaration || (s instanceof FunctionDeclaration f && !f.isIntrinsic()));
    }

    @Override
    public void visit(FunctionDeclaration declaration) {
        if (declaration.getReturnTypeExpr() == null)
            return;

        validateLastChildReturns(declaration.getBody()).ifPresent(offendingStatement -> {
            var span = offendingStatement.getSpan();
            if (offendingStatement instanceof BlockStatement)
                report(new Span(span.source(), span.offsetEnd() - 1, span.offsetEnd()), "Missing return statement");
            else
                report(span, "Missing return statement");
        });
    }

    /**
     * Returns the innermost offending statement, or none if all paths may exit.
     *
     * @param block The block to check
     * @return The offending statement, or none if all paths may exit.
     */
    private static Optional<Statement> validateLastChildReturns(BlockStatement block) {
        if (block.getStatements().isEmpty())
            return Optional.of(block);

        var lastChild = block.getStatements().get(block.getStatements().size() - 1);
        // TODO: Switch preview disabled
        if (lastChild instanceof BlockStatement blockStatement) {
            return validateLastChildReturns(blockStatement);
        } else if (lastChild instanceof IfStatement ifStatement) {
            return validateLastChildReturns(ifStatement.getBody()).or(() -> {
                if (ifStatement.getElseStatement() == null)
                    return Optional.of(ifStatement);
                return validateLastChildReturns(ifStatement.getElseStatement());
            });
        } else if (lastChild instanceof ReturnStatement) {
            return Optional.empty();
        }

        return Optional.of(lastChild);
    }
}
