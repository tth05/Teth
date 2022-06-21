package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class DeclarationStack {

    private final Deque<Scope> stack = new ArrayDeque<>();

    public Statement resolveIdentifier(String ident) {
        for (var it = this.stack.descendingIterator(); it.hasNext(); ) {
            var scope = it.next();
            var decl = scope.declarations.get(ident);
            if (decl != null)
                return decl;

            if (!scope.subScope)
                break;
        }

        // Check top level declarations
        //noinspection ConstantConditions
        return this.stack.peekFirst().declarations.get(ident);
    }

    public void addDeclaration(String value, Statement declaration) {
        this.stack.getLast().declarations.put(value, declaration);
    }

    public void beginScope(boolean subScope) {
        this.stack.addLast(new Scope(subScope));
    }

    public void endScope() {
        if (this.stack.size() < 2)
            throw new IllegalStateException("Cannot end scope");

        this.stack.removeLast();
    }

    private final class Scope {

        private final Map<String, Statement> declarations = new HashMap<>();
        private final boolean subScope;

        private Scope(boolean subScope) {
            this.subScope = subScope;
        }
    }
}