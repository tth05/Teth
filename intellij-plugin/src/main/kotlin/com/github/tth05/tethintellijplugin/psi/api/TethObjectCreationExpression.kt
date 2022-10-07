package com.github.tth05.tethintellijplugin.psi.api

interface TethObjectCreationExpression : TethExpression {

    val structName: String?
    val genericBounds: List<TethTypeExpression>
    val arguments: List<TethExpression>
}