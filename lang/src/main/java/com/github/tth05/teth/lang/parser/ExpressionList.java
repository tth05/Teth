package com.github.tth05.teth.lang.parser;

import com.github.tth05.teth.lang.parser.ast.Expression;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.span.ISpan;
import com.github.tth05.teth.lang.span.Span;
import com.github.tth05.teth.lang.util.ASTDumpBuilder;

import java.util.ArrayList;
import java.util.Collections;

public class ExpressionList extends ArrayList<Expression> implements IDumpable {

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

    public ISpan getSpanOrDefault(ISpan span) {
        if (this.size() > 0) {
            return Span.of(this.get(0).getSpan(), this.get(this.size() - 1).getSpan());
        } else {
            return span;
        }
    }

    public static ExpressionList of(Expression... statements) {
        var list = new ExpressionList();
        Collections.addAll(list, statements);
        return list;
    }
}
