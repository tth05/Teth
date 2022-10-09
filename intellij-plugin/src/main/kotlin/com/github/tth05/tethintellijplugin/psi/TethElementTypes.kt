package com.github.tth05.tethintellijplugin.psi

import com.github.tth05.tethintellijplugin.syntax.TethElementType

object TethElementTypes {

    val UNIT = TethElementType("UNIT")
    val STRUCT_DECLARATION = TethElementType("STRUCT_DECLARATION")
    val FIELD_DECLARATION = TethElementType("FIELD_DECLARATION")
    val FUNCTION_DECLARATION = TethElementType("FUNCTION_DECLARATION")
    val FUNCTION_PARAMETER_DECLARATION = TethElementType("FUNCTION_PARAMETER_DECLARATION")
    val GENERIC_PARAMETER_DECLARATION = TethElementType("GENERIC_PARAMETER_DECLARATION")
    val VARIABLE_DECLARATION = TethElementType("VARIABLE_DECLARATION")

    val BLOCK = TethElementType("BLOCK")
    val IF_STATEMENT = TethElementType("IF_STATEMENT")
    val LOOP_STATEMENT = TethElementType("LOOP_STATEMENT")
    val RETURN_STATEMENT = TethElementType("RETURN_STATEMENT")
    val USE_STATEMENT = TethElementType("USE_STATEMENT")

    val FUNCTION_INVOCATION = TethElementType("FUNCTION_INVOCATION")
    val BINARY_EXPRESSION = TethElementType("BINARY_EXPRESSION")
    val UNARY_EXPRESSION = TethElementType("UNARY_EXPRESSION")
    val GARBAGE_EXPRESSION = TethElementType("GARBAGE_EXPRESSION")
    val OBJECT_CREATION_EXPRESSION = TethElementType("OBJECT_CREATION_EXPRESSION")
    val MEMBER_ACCESS_EXPRESSION = TethElementType("MEMBER_ACCESS_EXPRESSION")
    val LIST_LITERAL_EXPRESSION = TethElementType("LIST_LITERAL_EXPRESSION")
    val LONG_LITERAL = TethElementType("LONG_LITERAL")
    val DOUBLE_LITERAL = TethElementType("DOUBLE_LITERAL")
    val STRING_LITERAL = TethElementType("STRING_LITERAL")
    val BOOLEAN_LITERAL = TethElementType("BOOLEAN_LITERAL")
    val IDENTIFIER_LITERAL = TethElementType("IDENTIFIER_LITERAL")

    val TYPE = TethElementType("TYPE")
}
