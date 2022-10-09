package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.*
import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class TethFunctionDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethFunctionDeclaration {
    override val genericParameters: List<TethGenericParameterDeclaration>
        get() = childrenOfType()
    override val parameters: List<TethFunctionParameterDeclaration>
        get() = childrenOfType()
    override val returnType: TethTypeExpression?
        get() = findChildByClass(TethTypeExpression::class.java)
    override val body: TethBlockStatement
        get() = findChildByClass(TethBlockStatement::class.java)!!
    override val isInstanceFunction: Boolean
        get() = parent is TethStructDeclaration
}