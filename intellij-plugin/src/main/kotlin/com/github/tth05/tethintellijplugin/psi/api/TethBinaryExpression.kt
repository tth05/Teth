package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiElement

interface TethBinaryExpression : TethExpression {

    val left: TethExpression
    val right: TethExpression
    val operatorToken: PsiElement
}