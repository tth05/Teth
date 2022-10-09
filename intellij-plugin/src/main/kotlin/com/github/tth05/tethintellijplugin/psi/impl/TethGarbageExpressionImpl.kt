package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethGarbageExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class TethGarbageExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethGarbageExpression {
}