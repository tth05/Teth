package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.BlockStatement;
import com.github.tth05.teth.lang.parser.ast.IfStatement;
import com.github.tth05.teth.lang.parser.ast.ReturnStatement;
import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.Optional;

public class ScopeExitHelper {

    /**
     * Returns the innermost offending statement, or none if all paths may exit.
     *
     * @param block The block to check
     * @return The offending statement, or none if all paths may exit.
     */
    public static Optional<Statement> doesExitInAllCases(BlockStatement block) {
        if (block.getStatements().isEmpty())
            return Optional.of(block);

        var child = block.getStatements().get(block.getStatements().size() - 1);
        return switch (child) {
            case BlockStatement blockStatement -> doesExitInAllCases(blockStatement);
            case IfStatement ifStatement -> doesExitInAllCases(ifStatement.getBody()).or(() -> {
                if (ifStatement.getElseStatement() == null)
                    return Optional.of(ifStatement);
                return doesExitInAllCases(ifStatement.getElseStatement());
            });
            case ReturnStatement ignored -> Optional.empty();
            case default -> Optional.of(child);
        };
    }
}
