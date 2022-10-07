package com.github.tth05.tethintellijplugin.psi.api

interface TethLoopStatement : TethStatement {

    val variableDeclarations: List<TethVariableDeclaration>
    val condition: TethExpression
    val body: TethBlockStatement
    val advanceStatement: TethStatement?
}