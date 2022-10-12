package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.api.TethNameIdentifierOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class TethNamedElement(node: ASTNode) : ASTWrapperPsiElement(node), TethNameIdentifierOwner {

    override fun setName(name: String): PsiElement = apply {
        nameIdentifier?.replace(FakeElementFactory(project).createIdentifierLiteralExpression(name))
    }

    override fun getTextOffset(): Int = nameIdentifier?.textOffset ?: super.getTextOffset()

    override fun getName(): String? = nameIdentifier?.text

    override fun getNameIdentifier(): PsiElement? = findChildByType(TethElementTypes.IDENTIFIER_LITERAL)
}