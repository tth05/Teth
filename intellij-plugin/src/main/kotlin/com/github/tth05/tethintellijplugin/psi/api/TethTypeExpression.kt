package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiElement

interface TethTypeExpression : PsiElement {

    val typeName: TethIdentifierLiteralExpression
    val genericArguments: List<TethTypeExpression>
}
