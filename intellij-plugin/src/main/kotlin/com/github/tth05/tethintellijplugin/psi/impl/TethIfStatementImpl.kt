package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethIfStatement
import com.github.tth05.tethintellijplugin.psi.api.TethStatement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

class TethIfStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethIfStatement {
    override val condition: TethExpression
        get() = firstChild as TethExpression
    override val body: TethStatement
        get() = PsiTreeUtil.getNextSiblingOfType(condition, TethStatement::class.java)!!
    override val elseBody: TethStatement?
        get() = PsiTreeUtil.getNextSiblingOfType(body, TethStatement::class.java)
}