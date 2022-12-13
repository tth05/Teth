package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.lang.lexer.TokenType
import com.github.tth05.tethintellijplugin.TethLanguage
import com.intellij.psi.tree.IElementType

object TethTokenTypes {

    val KEYWORD = TethTokenType("Keyword")
    val IDENTIFIER = TethTokenType("Identifier")
    val SEPARATOR = TethTokenType("Separator")
    val OPERATOR = TethTokenType("Operator")
    val L_CURLY_PAREN = TethTokenType("L_CURLY")
    val R_CURLY_PAREN = TethTokenType("R_CURLY")
    val L_PAREN = TethTokenType("L_PAREN")
    val R_PAREN = TethTokenType("R_PAREN")
    val L_SQUARE_BRACKET = TethTokenType("L_SQUARE_BRACKET")
    val R_SQUARE_BRACKET = TethTokenType("R_SQUARE_BRACKET")
    val LESS = TethTokenType("LESS")
    val LESS_PIPE = TethTokenType("LESS_PIPE")
    val GREATER = TethTokenType("GREATER")
    val COMMA = TethTokenType("COMMA")
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
            TokenType.KEYWORD_BREAK, TokenType.KEYWORD_CONTINUE, TokenType.KEYWORD_LOOP, TokenType.KEYWORD_STRUCT,
            TokenType.KEYWORD_NEW, TokenType.KEYWORD_RETURN, TokenType.KEYWORD_USE, TokenType.KEYWORD_NULL,
            TokenType.KEYWORD_INTRINSIC -> KEYWORD

            TokenType.COLON, TokenType.DOT, TokenType.STRING_LITERAL_CODE_END, TokenType.STRING_LITERAL_CODE_START -> SEPARATOR

            TokenType.L_CURLY_PAREN -> L_CURLY_PAREN
            TokenType.R_CURLY_PAREN -> R_CURLY_PAREN
            TokenType.L_PAREN -> L_PAREN
            TokenType.R_PAREN -> R_PAREN
            TokenType.L_SQUARE_BRACKET -> L_SQUARE_BRACKET
            TokenType.R_SQUARE_BRACKET -> R_SQUARE_BRACKET
            TokenType.LESS -> LESS
            TokenType.GREATER -> GREATER
            TokenType.LESS_PIPE -> LESS_PIPE
            TokenType.COMMA -> COMMA

            TokenType.AMPERSAND_AMPERSAND, TokenType.EQUAL, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
            TokenType.EQUAL_EQUAL, TokenType.PIPE_PIPE, TokenType.NOT_EQUAL, TokenType.MINUS, TokenType.PLUS,
            TokenType.MULTIPLY, TokenType.SLASH, TokenType.POW, TokenType.NOT -> OPERATOR

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