package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType

class TethTypeExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethTypeExpression {
    override val typeName: String?
        get() = findNotNullChildByType<PsiElement>(TethElementTypes.IDENTIFIER_LITERAL).text
    override val genericArguments: List<TethTypeExpression>
        get() = childrenOfType()
}