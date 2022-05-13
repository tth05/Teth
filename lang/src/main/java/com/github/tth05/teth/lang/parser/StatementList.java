package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.ArrayList;
import java.util.Collections;

public class StatementList extends ArrayList<Statement> implements IDumpable {

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("[").newLine().startBlock();
        forEach(s -> {
            s.dump(builder);
            builder.append(",").newLine();
        });
        builder.endBlock();
        builder.append("]");
    }

    public static StatementList of(Statement... statements) {
        var list = new StatementList();
        Collections.addAll(list, statements);
        return list;
    }
}
