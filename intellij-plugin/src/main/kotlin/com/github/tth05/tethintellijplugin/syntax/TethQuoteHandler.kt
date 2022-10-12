package com.github.tth05.tethintellijplugin.syntax

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.tree.TokenSet

class TethQuoteHandler : SimpleTokenSetQuoteHandler(TokenSet.create(TethTokenTypes.STRING_LITERAL))