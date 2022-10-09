package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethBlockStatement
import com.github.tth05.tethintellijplugin.psi.api.TethStatement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class TethBlockStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethBlockStatement {
    override val statements: List<TethStatement>
        get() = childrenOfType()
}