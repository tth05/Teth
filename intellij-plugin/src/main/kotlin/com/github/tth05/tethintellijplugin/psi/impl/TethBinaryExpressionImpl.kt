package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethBinaryExpression
import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class TethBinaryExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethBinaryExpression {
    override val left: TethExpression
        get() = findChildrenByClass(TethExpression::class.java)[0]
    override val right: TethExpression
        get() = findChildrenByClass(TethExpression::class.java)[0]
    override val operatorToken: PsiElement
        get() = findNotNullChildByType(TethTokenTypes.OPERATOR)
}