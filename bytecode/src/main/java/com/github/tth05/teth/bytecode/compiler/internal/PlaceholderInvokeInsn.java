package com.github.tth05.teth.bytecode.compiler.internal;

import com.github.tth05.teth.bytecode.op.IInstrunction;
import com.github.tth05.teth.bytecode.op.OpCodes;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;

public record PlaceholderInvokeInsn(FunctionDeclaration target) implements IInstrunction {

    public boolean isInstanceFunction() {
        return this.target.isInstanceFunction();
    }

    public int getParamCount() {
        return this.target.getParameters().size() + (this.isInstanceFunction() ? 1 : 0);
    }

    public boolean returnsValue() {
        return this.target.getReturnTypeExpr() != null;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.INVOKE;
    }

    @Override
    public String getDebugParametersString() {
        throw new UnsupportedOperationException();
    }
}
