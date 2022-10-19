package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiElement

interface TethVariableDeclaration : TethStatement, TethNameIdentifierOwner {

    val letKeyword: PsiElement?
    val type: TethTypeExpression?
    val initializer: TethExpression
}
