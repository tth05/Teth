package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethBinaryExpression
import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethUnaryExpression
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class TethUnaryExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethUnaryExpression {
    override val expression: TethExpression
        get() = findNotNullChildByClass(TethExpression::class.java)
    override val operatorToken: PsiElement
        get() = findNotNullChildByType(TethTokenTypes.OPERATOR)
}