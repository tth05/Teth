package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethIdentifierLiteralExpression
import com.github.tth05.tethintellijplugin.psi.api.TethObjectCreationExpression
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class TethObjectCreationExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethObjectCreationExpression {
    override val structNameIdentifier: TethIdentifierLiteralExpression
        get() = findNotNullChildByClass(TethIdentifierLiteralExpression::class.java)
    override val genericBounds: List<TethTypeExpression>
        get() = childrenOfType()
    override val arguments: List<TethExpression>
        get() = childrenOfType()
}