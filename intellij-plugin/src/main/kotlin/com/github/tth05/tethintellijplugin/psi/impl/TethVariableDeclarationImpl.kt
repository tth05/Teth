package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.github.tth05.tethintellijplugin.psi.api.TethVariableDeclaration
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendants
import com.intellij.psi.util.descendantsOfType
import com.intellij.psi.util.elementType

class TethVariableDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethVariableDeclaration {
    override val letKeyword: PsiElement
        get() = descendants { true }.first { it.elementType == TethTokenTypes.KEYWORD }
    override val type: TethTypeExpression?
        get() = findChildByClass(TethTypeExpression::class.java)
    override val initializer: TethExpression
        get() = type?.let { PsiTreeUtil.getNextSiblingOfType(it, TethExpression::class.java)!! }
            ?: nameIdentifier?.let { PsiTreeUtil.getNextSiblingOfType(it, TethExpression::class.java)!! }
            ?: findNotNullChildByClass(TethExpression::class.java)
}