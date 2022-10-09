package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface TethTypeExpression : PsiElement {

    val typeName: String?
    val genericArguments: List<TethTypeExpression>
}
