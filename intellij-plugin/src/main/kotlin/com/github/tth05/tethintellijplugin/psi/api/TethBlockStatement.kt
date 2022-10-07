package com.github.tth05.tethintellijplugin.psi.api

interface TethBlockStatement : TethStatement {

    val statements: List<TethStatement>
}