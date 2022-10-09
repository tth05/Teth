package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethExpression
import com.github.tth05.tethintellijplugin.psi.api.TethFunctionInvocationExpression
import com.github.tth05.tethintellijplugin.psi.api.TethTypeExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import com.intellij.util.containers.toMutableSmartList

class TethFunctionInvocationExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethFunctionInvocationExpression {
    override val target: TethExpression
        get() = firstChild as TethExpression
    override val genericArguments: List<TethTypeExpression>
        get() = childrenOfType()
    override val arguments: List<TethExpression>
        get() {
            val list = childrenOfType<TethExpression>().toMutableSmartList()
            list.removeFirst() // Remove target
            return list
        }
}