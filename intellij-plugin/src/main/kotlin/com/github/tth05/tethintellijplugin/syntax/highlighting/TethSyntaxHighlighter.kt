package com.github.tth05.tethintellijplugin.syntax.highlighting

import com.github.tth05.tethintellijplugin.syntax.TethLexer
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType


class TethSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = TethLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
        TethTokenTypes.COMMENT -> COMMENT_KEYS
        TethTokenTypes.IDENTIFIER -> IDENTIFIER_KEYS
        TethTokenTypes.KEYWORD -> KEYWORD_KEYS
        TethTokenTypes.STRING_LITERAL -> STRING_VALUE_KEYS
        TethTokenTypes.DOUBLE, TethTokenTypes.LONG -> NUMBER_VALUE_KEYS
        TethTokenTypes.SEPARATOR -> SEPARATOR_KEYS
        TethTokenTypes.OPERATOR -> OPERATOR_KEYS
        TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
        else -> TextAttributesKey.EMPTY_ARRAY
    }

    companion object {
        val SEPARATOR: TextAttributesKey =
            createTextAttributesKey("TETH_SEPARATOR", DefaultLanguageHighlighterColors.SEMICOLON)
        val OPERATOR: TextAttributesKey =
            createTextAttributesKey("TETH_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val IDENTIFIER: TextAttributesKey =
            createTextAttributesKey("TETH_IDENTIFIER", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
        val PARAMETER: TextAttributesKey =
            createTextAttributesKey("TETH_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
        val KEYWORD: TextAttributesKey =
            createTextAttributesKey("TETH_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING_VALUE: TextAttributesKey =
            createTextAttributesKey("TETH_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER_VALUE: TextAttributesKey =
            createTextAttributesKey("TETH_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val COMMENT: TextAttributesKey =
            createTextAttributesKey("TETH_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val FUNCTION_CALL: TextAttributesKey =
            createTextAttributesKey("TETH_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val TYPE: TextAttributesKey =
            createTextAttributesKey("TETH_TYPE", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
        val FIELD: TextAttributesKey =
            createTextAttributesKey("TETH_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val BAD_CHARACTER: TextAttributesKey =
            createTextAttributesKey("TETH_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val SEPARATOR_KEYS: Array<TextAttributesKey> = pack(SEPARATOR)
        private val OPERATOR_KEYS: Array<TextAttributesKey> = pack(OPERATOR)
        private val IDENTIFIER_KEYS: Array<TextAttributesKey> = pack(IDENTIFIER)
        private val KEYWORD_KEYS: Array<TextAttributesKey> = pack(KEYWORD)
        private val STRING_VALUE_KEYS: Array<TextAttributesKey> = pack(STRING_VALUE)
        private val NUMBER_VALUE_KEYS: Array<TextAttributesKey> = pack(NUMBER_VALUE)
        private val COMMENT_KEYS: Array<TextAttributesKey> = pack(COMMENT)
        private val BAD_CHARACTER_KEYS: Array<TextAttributesKey> = pack(BAD_CHARACTER)
    }
}