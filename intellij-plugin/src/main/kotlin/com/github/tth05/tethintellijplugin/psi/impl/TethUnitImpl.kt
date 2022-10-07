package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethStatement
import com.github.tth05.tethintellijplugin.psi.api.TethUnit
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class TethUnitImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethUnit {

    override val statements: List<TethStatement>
        get() = childrenOfType()
}