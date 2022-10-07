package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethTypeExpression {

    val typeName: String?
    val genericArguments: List<TethTypeExpression>
}
