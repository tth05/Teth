package com.github.tth05.tethintellijplugin.syntax.brace

import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class TethBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        val PAIRS= arrayOf(
            BracePair(TethTokenTypes.L_PAREN, TethTokenTypes.R_PAREN, false),
            BracePair(TethTokenTypes.L_SQUARE_BRACKET, TethTokenTypes.R_SQUARE_BRACKET, false),
            BracePair(TethTokenTypes.LESS_PIPE, TethTokenTypes.GREATER, false),
            BracePair(TethTokenTypes.LESS, TethTokenTypes.GREATER, false),
            BracePair(TethTokenTypes.L_CURLY_PAREN, TethTokenTypes.R_CURLY_PAREN, true),
        )
    }
}