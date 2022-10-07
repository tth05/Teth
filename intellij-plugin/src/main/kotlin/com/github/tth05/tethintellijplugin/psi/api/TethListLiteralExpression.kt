package com.github.tth05.tethintellijplugin.psi.api

interface TethListLiteralExpression : TethExpression {

    val initializers: List<TethExpression>
}