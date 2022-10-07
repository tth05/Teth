package com.github.tth05.tethintellijplugin.psi

import com.github.tth05.tethintellijplugin.syntax.TethElementType

object TethElementTypes {

    val UNIT = TethElementType("UNIT")
    val VARIABLE_DECLARATION = TethElementType("VARIABLE_DECLARATION")
    val TYPE_EXPRESSION = TethElementType("TYPE_EXPRESSION")
    val BINARY_EXPRESSION = TethElementType("BINARY_EXPRESSION")
    val LONG_LITERAL = TethElementType("LONG_LITERAL")
    val IDENTIFIER_LITERAL = TethElementType("IDENTIFIER_LITERAL")

}