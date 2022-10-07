package com.github.tth05.tethintellijplugin.psi.api

interface TethFunctionInvocationExpression : TethExpression {

    val target: TethExpression
    val genericArguments: List<TethTypeExpression>
    val arguments: List<TethExpression>
}