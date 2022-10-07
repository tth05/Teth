package com.github.tth05.tethintellijplugin.psi.api

interface TethReturnStatement : TethStatement {

    val expression: TethExpression?
}