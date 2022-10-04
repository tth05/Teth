package com.github.tth05.tethintellijplugin.syntax

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * Simple parser which turns the given tokens into a flat AST
 */
class TethParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            val tokenType = builder.tokenType!!

            val marker = builder.mark()
            builder.advanceLexer()
            marker.done(tokenType)
        }

        rootMarker.done(TethTokenTypes.ROOT)

        return builder.treeBuilt
    }
}