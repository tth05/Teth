package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethIdentifierLiteralExpression
import com.github.tth05.tethintellijplugin.psi.api.TethMemberAccessExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class TethMemberAccessExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethMemberAccessExpression {
    override val target: TethExpression
        get() = findNotNullChildByClass(TethExpression::class.java)
    override val memberNameIdentifier: TethIdentifierLiteralExpression
        get() = findNotNullChildByClass(TethIdentifierLiteralExpression::class.java)
}