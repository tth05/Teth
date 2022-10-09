package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethFieldDeclaration
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.intellij.lang.ASTNode

class TethFieldDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethFieldDeclaration {
    override val type: TethTypeExpression
        get() = findNotNullChildByClass(TethTypeExpression::class.java)
}