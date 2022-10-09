package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.api.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType

class TethLongLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethLongLiteralExpression

class TethDoubleLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethDoubleLiteralExpression

class TethStringLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethStringLiteralExpression

class TethBooleanLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethBooleanLiteralExpression

class TethIdentifierLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethIdentifierLiteralExpression {
    override val identifier: String?
        get() = text.emptyToNull()
}

class TethListLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethListLiteralExpression {
    override val initializers: List<TethExpression>
        get() = childrenOfType()
}

class TethTypeExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethTypeExpression {
    override val typeName: String?
        get() = findChildByType<PsiElement>(TethElementTypes.IDENTIFIER_LITERAL)?.text?.emptyToNull()
    override val genericArguments: List<TethTypeExpression>
        get() = childrenOfType()
}

private fun String.emptyToNull() = ifEmpty { null }