package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiElement

interface TethUnaryExpression : TethExpression {

    val expression: TethExpression
    val operatorToken: PsiElement
}