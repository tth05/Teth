package com.github.tth05.tethintellijplugin.syntax.brace

import com.github.tth05.tethintellijplugin.TethLanguage
import com.github.tth05.tethintellijplugin.TethLanguageFileType
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.codeInsight.highlighting.PairedBraceAndAnglesMatcher
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class TethPairedBraceMatcher : PairedBraceAndAnglesMatcher(TethBraceMatcher(), TethLanguage.INSTANCE, TethLanguageFileType.INSTANCE, ALLOWED_INSIDE_ANGLES) {

    override fun lt(): IElementType = TethTokenTypes.LESS

    override fun gt(): IElementType = TethTokenTypes.GREATER

    companion object {
        val ALLOWED_INSIDE_ANGLES = TokenSet.create(TethTokenTypes.IDENTIFIER, TethTokenTypes.COMMA, TokenType.WHITE_SPACE)
    }
}