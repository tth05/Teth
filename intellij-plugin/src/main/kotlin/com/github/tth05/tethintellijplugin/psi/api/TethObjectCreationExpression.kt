package com.github.tth05.tethintellijplugin.psi.api

interface TethObjectCreationExpression : TethExpression {

    val structNameIdentifier: TethIdentifierLiteralExpression
    val genericBounds: List<TethTypeExpression>
    val arguments: List<TethExpression>
}