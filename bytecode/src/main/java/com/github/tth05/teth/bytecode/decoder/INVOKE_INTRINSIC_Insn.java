package com.github.tth05.teth.bytecode.decoder;

import com.github.tth05.teth.bytecode.compiler.OpCodes;
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration;

public class INVOKE_INTRINSIC_Insn implements IInstrunction {

    private final FunctionDeclaration functionDeclaration;

    public INVOKE_INTRINSIC_Insn(FunctionDeclaration functionDeclaration) {
        this.functionDeclaration = functionDeclaration;
    }

    public FunctionDeclaration getFunctionDeclaration() {
        return this.functionDeclaration;
    }

    @Override
    public byte getOpCode() {
        return OpCodes.INVOKE_INTRINSIC;
    }

    @Override
    public String getDebugParametersString() {
        return this.getFunctionDeclaration().getNameExpr() + "";
    }
}
