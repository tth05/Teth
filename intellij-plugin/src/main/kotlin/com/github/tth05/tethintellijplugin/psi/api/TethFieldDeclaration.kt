package com.github.tth05.tethintellijplugin.psi.api

interface TethFieldDeclaration : TethStatement, TethNameIdentifierOwner {

    val type: TethTypeExpression
}
