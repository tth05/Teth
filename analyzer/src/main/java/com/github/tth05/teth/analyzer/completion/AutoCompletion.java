package com.github.tth05.teth.analyzer.completion;

import com.github.tth05.teth.analyzer.Analyzer;
import com.github.tth05.teth.analyzer.prelude.Prelude;
import com.github.tth05.teth.lang.parser.ASTUtil;
import com.github.tth05.teth.lang.parser.SourceFileUnit;
import com.github.tth05.teth.lang.parser.ast.IdentifierExpression;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.parser.ast.TypeExpression;

import java.util.*;
import java.util.stream.Collectors;

public class AutoCompletion {

    private final Analyzer analyzer;
    private final SourceFileUnit unit;

    public AutoCompletion(Analyzer analyzer, SourceFileUnit unit) {
        this.analyzer = analyzer;
        this.unit = unit;
    }

    public List<CompletionItem> complete(int offset) {
        var stack = new ArrayDeque<Statement>();
        ASTUtil.walkNodesAtOffset(this.unit.getStatements(), offset, true, stack::push);

        if (stack.isEmpty())
            return Collections.emptyList();

        var results = new ArrayList<CompletionItem>();

        var first = stack.pop();
        if (first instanceof IdentifierExpression expr) {
            if (expr.getValue() == null)
                return Collections.emptyList();

            var text = expr.getValue().substring(0, offset - expr.getSpan().offset());
            if (stack.peek() instanceof TypeExpression) {
                addGlobalStructs(text, results);
            } else {
                addGlobalFunctions(text, results);
            }
        }

        return results;
    }

    private static void addGlobalFunctions(String text, List<CompletionItem> results) {
        Arrays.stream(Prelude.getGlobalFunctions())
                .filter(f -> f.getNameExpr().getValue().startsWith(text))
                .map(f -> new CompletionItem(f.getNameExpr().getValue(), CompletionItem.Type.FUNCTION))
                .collect(Collectors.toCollection(() -> results));
    }

    private static void addGlobalStructs(String text, List<CompletionItem> results) {
        Arrays.stream(Prelude.getGlobalStructs())
                .filter(s -> s.getNameExpr().getValue().startsWith(text))
                .map(s -> new CompletionItem(s.getNameExpr().getValue(), CompletionItem.Type.STRUCT))
                .collect(Collectors.toCollection(() -> results));
    }
}
