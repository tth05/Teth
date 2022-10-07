package com.github.tth05.tethintellijplugin

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.syntax.TethLexer
import com.github.tth05.tethintellijplugin.syntax.TethParser
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.github.tth05.tethintellijplugin.psi.impl.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class TethParserDefinition : ParserDefinition {
    // TODO: Migrate to better AST representation. Will allow reference and rename stuff
    //  3. Convert teth AST to custom PSI tree
    //  4. Save source file unit (and problems) as key in file psi to allow annotator to use it
    override fun createLexer(project: Project?): Lexer = TethLexer()

    override fun createParser(project: Project?): PsiParser = TethParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENT_TOKENS

    override fun getStringLiteralElements(): TokenSet = STRING_LITERAL_TOKENS

    override fun createElement(node: ASTNode?): PsiElement = when (val type = node!!.elementType) {
        TethTokenTypes.COMMENT -> PsiCommentImpl(type, node.text)
        TethElementTypes.UNIT -> TethUnitImpl(node)
        TethElementTypes.VARIABLE_DECLARATION -> TethVariableDeclarationImpl(node)
        TethElementTypes.BINARY_EXPRESSION -> TethBinaryExpressionImpl(node)
        TethElementTypes.TYPE_EXPRESSION -> TethTypeExpressionImpl(node)
        TethElementTypes.LONG_LITERAL -> TethLongLiteralExpressionImpl(node)
        TethElementTypes.IDENTIFIER_LITERAL -> TethIdentifierLiteralExpressionImpl(node)
        else -> ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = TethPsiFile(viewProvider)

    companion object {
        val FILE = IFileElementType(TethLanguage.INSTANCE)

        val COMMENT_TOKENS = TokenSet.create(TethTokenTypes.COMMENT)
        val STRING_LITERAL_TOKENS = TokenSet.create(TethTokenTypes.STRING_LITERAL)
    }
}