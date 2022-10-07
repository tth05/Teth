package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethFieldDeclaration : TethStatement, PsiNameIdentifierOwner {

    val type: TethTypeExpression
}
