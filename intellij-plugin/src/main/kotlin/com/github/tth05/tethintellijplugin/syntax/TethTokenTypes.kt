package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.lang.lexer.TokenType
import com.github.tth05.tethintellijplugin.TethLanguage
import com.intellij.psi.tree.IElementType

object TethTokenTypes {

    val KEYWORD = TethTokenType("Keyword")
    val IDENTIFIER = TethTokenType("Identifier")
    val SEPARATOR = TethTokenType("Separator")
    val OPERATOR = TethTokenType("Operator")
    val STRING_LITERAL = TethTokenType("String literal")
    val DOUBLE = TethTokenType("Double literal")
    val LONG = TethTokenType("Long literal")
    val COMMENT = TethTokenType("Comment")

    fun of(type: TokenType): IElementType? {
        return when (type) {
            TokenType.IDENTIFIER -> IDENTIFIER
            TokenType.STRING_LITERAL -> STRING_LITERAL
            TokenType.DOUBLE_LITERAL -> DOUBLE
            TokenType.LONG_LITERAL -> LONG
            TokenType.BOOLEAN_LITERAL -> KEYWORD
            TokenType.KEYWORD_IF, TokenType.KEYWORD_ELSE, TokenType.KEYWORD_FN, TokenType.KEYWORD_LET,
            TokenType.KEYWORD_LOOP, TokenType.KEYWORD_STRUCT, TokenType.KEYWORD_NEW, TokenType.KEYWORD_RETURN,
            TokenType.KEYWORD_USE -> KEYWORD

            TokenType.COLON, TokenType.COMMA, TokenType.L_CURLY_PAREN, TokenType.R_CURLY_PAREN, TokenType.L_PAREN,
            TokenType.R_PAREN, TokenType.DOT, TokenType.LESS_PIPE, TokenType.L_SQUARE_BRACKET,
            TokenType.R_SQUARE_BRACKET, TokenType.STRING_LITERAL_CODE_END,
            TokenType.STRING_LITERAL_CODE_START -> SEPARATOR

            TokenType.AMPERSAND_AMPERSAND, TokenType.EQUAL, TokenType.GREATER_EQUAL, TokenType.GREATER, TokenType.LESS,
            TokenType.LESS_EQUAL, TokenType.EQUAL_EQUAL, TokenType.PIPE_PIPE, TokenType.NOT_EQUAL, TokenType.MINUS,
            TokenType.PLUS, TokenType.MULTIPLY, TokenType.SLASH, TokenType.POW, TokenType.NOT -> OPERATOR

            TokenType.COMMENT -> COMMENT
            TokenType.WHITESPACE, TokenType.LINE_BREAK -> com.intellij.psi.TokenType.WHITE_SPACE
            TokenType.INVALID -> com.intellij.psi.TokenType.BAD_CHARACTER
            else -> throw IllegalArgumentException("Unknown token type: $type")
        }
    }
}

class TethTokenType(debugName: String) : IElementType(debugName, TethLanguage.INSTANCE) {
    override fun toString(): String = "TethTokenType." + super.toString()
}

class TethElementType(debugName: String) : IElementType(debugName, TethLanguage.INSTANCE) {
    override fun toString(): String = "TethElementType." + super.toString()
}