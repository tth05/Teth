package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethStructDeclaration : TethStatement, TethNameIdentifierOwner {

    val genericParameters: List<TethGenericParameterDeclaration>
    val fields: List<TethFieldDeclaration>
    val functions: List<TethFunctionDeclaration>
}