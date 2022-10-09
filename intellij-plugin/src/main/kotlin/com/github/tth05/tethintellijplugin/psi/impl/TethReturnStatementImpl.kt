package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethReturnStatement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class TethReturnStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethReturnStatement {
    override val expression: TethExpression?
        get() = findChildByClass(TethExpression::class.java)
}