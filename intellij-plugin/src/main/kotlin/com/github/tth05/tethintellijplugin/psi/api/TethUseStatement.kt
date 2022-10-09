package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethUseStatement : TethStatement {

    val moduleName: TethIdentifierLiteralExpression
    val imports: List<TethIdentifierLiteralExpression>
}