package com.github.tth05.tethintellijplugin.psi.api

interface TethIfStatement : TethStatement {

    val condition: TethExpression
    val body: TethStatement
    val elseBody: TethStatement?
}