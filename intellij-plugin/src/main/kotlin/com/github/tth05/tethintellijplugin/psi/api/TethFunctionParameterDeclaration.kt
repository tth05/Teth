package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethFunctionParameterDeclaration : TethNameIdentifierOwner {

    val type: TethTypeExpression
}
