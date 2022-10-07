package com.github.tth05.tethintellijplugin.psi.api

import com.intellij.psi.PsiNameIdentifierOwner

interface TethFunctionDeclaration : TethStatement, PsiNameIdentifierOwner {

    val genericParameters: List<TethGenericParameterDeclaration>
    val parameters: List<TethFunctionParameterDeclaration>
    val returnType: TethTypeExpression?
    val body: TethBlockStatement

    val isInstanceFunction: Boolean
}