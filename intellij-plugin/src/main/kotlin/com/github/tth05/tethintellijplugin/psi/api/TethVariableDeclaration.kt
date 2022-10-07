package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethVariableDeclaration : TethStatement, PsiNameIdentifierOwner {

    val type: TethTypeExpression?
    val initializer: TethExpression
}
