package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethUseStatement : TethStatement {

    val moduleName: String?
    val imports: List<String?>
}