package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.tethintellijplugin.TethLanguage
import com.github.tth05.tethintellijplugin.TethLexer
import com.intellij.lexer.FlexAdapter
import com.intellij.psi.tree.IElementType

class TethTokenType(debugName: String) : IElementType(debugName, TethLanguage.INSTANCE) {
    override fun toString(): String = "TethTokenType." + super.toString()
}

class TethElementType(debugName: String) : IElementType(debugName, TethLanguage.INSTANCE) {
    override fun toString(): String = "TethElementType." + super.toString()
}

class TethLexerAdapter : FlexAdapter(TethLexer(null))