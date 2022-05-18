package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.ast.Statement;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class StatementList extends ArrayList<Statement> implements IDumpable {

    public StatementList() {
    }

    public StatementList(Collection<? extends Statement> c) {
        super(c);
    }

    @Override
    public void dump(ASTDumpBuilder builder) {
        builder.append("[").newLine().startBlock();
        for (int i = 0; i < this.size(); i++) {
            Statement s = this.get(i);
            s.dump(builder);
            if (i < size() - 1)
                builder.append(",");
            builder.newLine();
        }
        builder.endBlock().append("]");
    }

    public static StatementList of(Statement... statements) {
        var list = new StatementList();
        Collections.addAll(list, statements);
        return list;
    }
}
