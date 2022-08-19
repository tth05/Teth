package com.github.tth05.teth.analyzer.visitor;

import com.github.tth05.teth.analyzer.ValidationException;
import com.github.tth05.teth.lang.parser.ASTVisitor;
import com.github.tth05.teth.lang.parser.ast.*;

import java.util.Optional;

public class ReturnStatementVerifier extends ASTVisitor {

    public ReturnStatementVerifier() {
        // Skips imports, top level code etc.
        setBlockStatementFilter(s -> s instanceof StructDeclaration || (s instanceof FunctionDeclaration f && !f.isIntrinsic()));
    }

    @Override
    public void visit(FunctionDeclaration declaration) {
        if (declaration.getReturnTypeExpr() == null)
            return;

        validateLastChildReturns(declaration.getBody()).ifPresent(offendingStatement -> {
            throw new ValidationException(offendingStatement.getSpan(), "Missing return statement");
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
        return switch (lastChild) {
            case BlockStatement blockStatement -> validateLastChildReturns(blockStatement);
            case IfStatement ifStatement -> validateLastChildReturns(ifStatement.getBody()).or(() -> {
                if (ifStatement.getElseStatement() == null)
                    return Optional.of(ifStatement);
                return validateLastChildReturns(ifStatement.getElseStatement());
            });
            case ReturnStatement ignored -> Optional.empty();
            case default -> Optional.of(lastChild);
        };
    }
}
