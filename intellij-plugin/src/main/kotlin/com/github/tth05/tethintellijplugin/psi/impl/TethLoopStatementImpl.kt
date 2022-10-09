package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType

class TethLoopStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethLoopStatement {
    override val variableDeclarations: List<TethVariableDeclaration>
        get() = childrenOfType()
    override val condition: TethExpression
        get() = findNotNullChildByClass(TethExpression::class.java)
    override val body: TethBlockStatement
        get() = findNotNullChildByClass(TethBlockStatement::class.java)
    override val advanceStatement: TethStatement?
        get() = PsiTreeUtil.getNextSiblingOfType(body, TethStatement::class.java)
}