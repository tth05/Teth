package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.github.tth05.tethintellijplugin.psi.api.TethVariableDeclaration
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

class TethVariableDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethVariableDeclaration {
    override val type: TethTypeExpression
        get() = findNotNullChildByClass(TethTypeExpression::class.java)
    override val initializer: TethExpression
        get() = PsiTreeUtil.getNextSiblingOfType(type, TethExpression::class.java)!!
}