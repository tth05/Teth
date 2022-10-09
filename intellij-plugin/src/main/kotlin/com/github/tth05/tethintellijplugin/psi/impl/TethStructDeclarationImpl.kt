package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethFieldDeclaration
import com.github.tth05.tethintellijplugin.psi.api.TethFunctionDeclaration
import com.github.tth05.tethintellijplugin.psi.api.TethGenericParameterDeclaration
import com.github.tth05.tethintellijplugin.psi.api.TethStructDeclaration
import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class TethStructDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethStructDeclaration {
    override val genericParameters: List<TethGenericParameterDeclaration>
        get() = childrenOfType()
    override val fields: List<TethFieldDeclaration>
        get() = childrenOfType()
    override val functions: List<TethFunctionDeclaration>
        get() = childrenOfType()
}