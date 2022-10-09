package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethFunctionParameterDeclaration
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.intellij.lang.ASTNode

class TethFunctionParameterDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethFunctionParameterDeclaration {
    override val type: TethTypeExpression
        get() = findNotNullChildByClass(TethTypeExpression::class.java)
}