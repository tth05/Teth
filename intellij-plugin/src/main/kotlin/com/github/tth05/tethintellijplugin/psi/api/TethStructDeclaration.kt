package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethStructDeclaration : TethStatement, PsiNameIdentifierOwner {

    val genericParameters: List<TethGenericParameterDeclaration>
    val fields: List<TethFieldDeclaration>
    val functions: List<TethFunctionDeclaration>
}