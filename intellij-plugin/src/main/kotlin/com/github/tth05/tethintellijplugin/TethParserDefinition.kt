package com.github.tth05.tethintellijplugin

import com.github.tth05.tethintellijplugin.syntax.TethLexerAdapter
import com.github.tth05.tethintellijplugin.syntax.TethParser
import com.github.tth05.tethintellijplugin.syntax.TethTypes
import com.github.tth05.tethintellijplugin.syntax.psi.TethPsiFile
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class TethParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = TethLexerAdapter()

    override fun createParser(project: Project?): PsiParser = TethParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createElement(node: ASTNode?): PsiElement = TethTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = TethPsiFile(viewProvider)

    companion object {
        val FILE = IFileElementType(TethLanguage.INSTANCE)
    }
}