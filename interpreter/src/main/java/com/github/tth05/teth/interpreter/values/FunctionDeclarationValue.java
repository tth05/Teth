package com.github.tth05.teth.interpreter.values;

import com.github.tth05.teth.interpreter.Interpreter;
import com.github.tth05.teth.lang.parser.Type;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;

public class FunctionDeclarationValue extends AbstractFunction implements IValue {

    private final FunctionDeclaration declaration;

    public FunctionDeclarationValue(FunctionDeclaration declaration) {
        super(declaration.getName(), false, declaration.getParameters().stream().map(FunctionDeclaration.Parameter::type).toArray(Type[]::new));
        this.declaration = declaration;
    }

    @Override
    public IValue invoke(Interpreter interpreter, IValue... args) {
        return interpreter.callFunction(this.declaration, args);
    }

    @Override
    public IValue copy() {
        // Immutable, no copy needed
        return this;
    }

    @Override
    public String getDebugString() {
        return this.declaration.getName() + "()";
    }

    @Override
    public Type getType() {
        return Type.FUNCTION;
    }
}
