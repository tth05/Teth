package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.github.tth05.tethintellijplugin.psi.api.TethVariableDeclaration
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType

class TethVariableDeclarationImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethVariableDeclaration {
    override val type: TethTypeExpression?
        get() = findChildByClass(TethTypeExpression::class.java)
    override val initializer: TethExpression
        get() = findNotNullChildByClass(TethExpression::class.java)

    override fun setName(name: String): PsiElement {
        TODO("Not yet implemented")
    }

    override fun getNameIdentifier(): PsiElement = findNotNullChildByType(TethElementTypes.IDENTIFIER_LITERAL)
}