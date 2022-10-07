package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethIdentifierLiteralExpression
import com.github.tth05.tethintellijplugin.psi.api.TethLongLiteralExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class TethLongLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethLongLiteralExpression

class TethIdentifierLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethIdentifierLiteralExpression {
    override val identifier: String?
        get() = text
}