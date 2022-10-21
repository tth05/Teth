package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IHasName;
import com.github.tth05.teth.lang.parser.ast.Statement;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ScopeStack {

    private final Deque<Scope> stack = new ArrayDeque<>();

    public Statement resolveIdentifier(String ident) {
        if (ident == null)
            return null;

        var pastSubScope = false;
        for (var it = this.stack.descendingIterator(); it.hasNext(); ) {
            var scope = it.next();
            if (!pastSubScope) {
                var decl = scope.declarations.get(ident);
                if (decl != null)
                    return decl;

                if (!scope.subScope)
                    pastSubScope = true;
            }

            var owner = scope.owner;
            if (pastSubScope && owner instanceof IHasName named && ident.equals(named.getNameExpr().getValue()) && // Check name matches
                (!(owner instanceof FunctionDeclaration func) || !func.isInstanceFunction())) // Filter instance functions
                return owner;
        }

        // Check top level declarations
        //noinspection ConstantConditions
        return this.stack.peekFirst().declarations.get(ident);
    }

    public <T extends Statement> T getClosestOfType(Class<T> clazz) {
        var it = this.stack.descendingIterator();
        while (it.hasNext()) {
            var scope = it.next();
            if (clazz.isInstance(scope.owner))
                return (T) scope.owner;

            if (!scope.subScope)
                break;
        }

        return null;
    }

    public <T extends Statement> T getCurrentOfType(Class<T> clazz) {
        var owner = this.stack.getLast().owner;
        return clazz.isInstance(this.stack.getLast().owner) ? (T) owner : null;
    }

    public int countScopesOfType(Class<? extends Statement> clazz) {
        var count = 0;
        for (var el : this.stack) {
            if (clazz.isInstance(el.owner))
                count++;
        }

        return count;
    }

    public void addDeclaration(String value, Statement declaration) {
        this.stack.getLast().declarations.put(value, declaration);
    }

    public void beginScope(Statement owner) {
        this.stack.addLast(new Scope(owner, false));
    }

    public void beginSubScope(Statement owner) {
        this.stack.addLast(new Scope(owner, true));
    }

    public void endScope() {
        if (this.stack.size() < 2)
            throw new IllegalStateException("Cannot end global scope");

        this.stack.removeLast();
    }

    private static final class Scope {

        private final Map<String, Statement> declarations = new HashMap<>();
        private final Statement owner;
        private final boolean subScope;

        private Scope(Statement owner, boolean subScope) {
            this.owner = owner;
            this.subScope = subScope;
        }
    }
}
