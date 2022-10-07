package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiElement

interface TethUnit : PsiElement {

    val statements: List<TethStatement>
}