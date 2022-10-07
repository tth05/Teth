package com.github.tth05.tethintellijplugin.psi.api

interface TethMemberAccessExpression : TethExpression {

    val target: TethExpression
    val memberName: String?
}