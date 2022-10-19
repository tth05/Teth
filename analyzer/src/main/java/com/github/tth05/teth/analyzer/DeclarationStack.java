package com.github.tth05.teth.analyzer;

import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;
import com.github.tth05.teth.lang.parser.ast.IHasName;
import com.github.tth05.teth.lang.parser.ast.Statement;
import com.github.tth05.teth.lang.parser.ast.StructDeclaration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class DeclarationStack {

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
            if (pastSubScope && owner != null && ident.equals(owner.getNameExpr().getValue()) && // Check name matches
                (!(owner instanceof FunctionDeclaration func) || !func.isInstanceFunction())) // Filter instance functions
                return (Statement) owner;
        }

        // Check top level declarations
        //noinspection ConstantConditions
        return this.stack.peekFirst().declarations.get(ident);
    }

    public StructDeclaration getEnclosingStruct() {
        var it = this.stack.descendingIterator();
        while (it.hasNext()) {
            var scope = it.next();
            if (scope.owner instanceof StructDeclaration s)
                return s;

            if (!scope.subScope)
                break;
        }

        return (it.hasNext() && it.next().owner instanceof StructDeclaration s) ? s : null;
    }

    public void addDeclaration(String value, Statement declaration) {
        this.stack.getLast().declarations.put(value, declaration);
    }

    public void beginSubScope() {
        this.stack.addLast(new Scope(null, true));
    }

    public void beginStructScope(StructDeclaration struct) {
        this.stack.addLast(new Scope(struct, false));
    }

    public void beginFunctionScope(FunctionDeclaration function) {
        this.stack.addLast(new Scope(function, false));
    }

    public void endScope() {
        if (this.stack.size() < 2)
            throw new IllegalStateException("Cannot end scope");

        this.stack.removeLast();
    }

    private static final class Scope {

        private final Map<String, Statement> declarations = new HashMap<>();
        private final IHasName owner;
        private final boolean subScope;

        private Scope(IHasName owner, boolean subScope) {
            this.owner = owner;
            this.subScope = subScope;
        }
    }
}
