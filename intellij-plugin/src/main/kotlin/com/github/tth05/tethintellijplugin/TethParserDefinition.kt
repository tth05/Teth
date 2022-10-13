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

//TODO:
// - Find usages
// - Inlay hints
// - Parameter info
// - Auto-completin
class TethParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = TethLexer()

    override fun createParser(project: Project?): PsiParser = TethParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENT_TOKENS

    override fun getStringLiteralElements(): TokenSet = STRING_LITERAL_TOKENS

    override fun createElement(node: ASTNode?): PsiElement = when (node!!.elementType) {
        TethElementTypes.UNIT -> TethUnitImpl(node)
        TethElementTypes.STRUCT_DECLARATION -> TethStructDeclarationImpl(node)
        TethElementTypes.FIELD_DECLARATION -> TethFieldDeclarationImpl(node)
        TethElementTypes.FUNCTION_DECLARATION -> TethFunctionDeclarationImpl(node)
        TethElementTypes.FUNCTION_PARAMETER_DECLARATION -> TethFunctionParameterDeclarationImpl(node)
        TethElementTypes.GENERIC_PARAMETER_DECLARATION -> TethGenericParameterDeclarationImpl(node)
        TethElementTypes.VARIABLE_DECLARATION -> TethVariableDeclarationImpl(node)

        TethElementTypes.BLOCK -> TethBlockStatementImpl(node)
        TethElementTypes.IF_STATEMENT -> TethIfStatementImpl(node)
        TethElementTypes.LOOP_STATEMENT -> TethLoopStatementImpl(node)
        TethElementTypes.RETURN_STATEMENT -> TethReturnStatementImpl(node)
        TethElementTypes.USE_STATEMENT -> TethUseStatementImpl(node)

        TethElementTypes.FUNCTION_INVOCATION -> TethFunctionInvocationExpressionImpl(node)
        TethElementTypes.BINARY_EXPRESSION -> TethBinaryExpressionImpl(node)
        TethElementTypes.UNARY_EXPRESSION -> TethUnaryExpressionImpl(node)
        TethElementTypes.GARBAGE_EXPRESSION -> TethGarbageExpressionImpl(node)
        TethElementTypes.OBJECT_CREATION_EXPRESSION -> TethObjectCreationExpressionImpl(node)
        TethElementTypes.MEMBER_ACCESS_EXPRESSION -> TethMemberAccessExpressionImpl(node)
        TethElementTypes.LIST_LITERAL_EXPRESSION -> TethListLiteralExpressionImpl(node)
        TethElementTypes.LONG_LITERAL -> TethLongLiteralExpressionImpl(node)
        TethElementTypes.DOUBLE_LITERAL -> TethDoubleLiteralExpressionImpl(node)
        TethElementTypes.STRING_LITERAL -> TethStringLiteralExpressionImpl(node)
        TethElementTypes.BOOLEAN_LITERAL -> TethBooleanLiteralExpressionImpl(node)
        TethElementTypes.IDENTIFIER_LITERAL -> TethIdentifierLiteralExpressionImpl(node)

        TethElementTypes.TYPE -> TethTypeExpressionImpl(node)
        else -> ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = TethPsiFile(viewProvider)

    companion object {
        val FILE = IFileElementType(TethLanguage.INSTANCE)

        val COMMENT_TOKENS = TokenSet.create(TethTokenTypes.COMMENT)
        val STRING_LITERAL_TOKENS = TokenSet.create(TethTokenTypes.STRING_LITERAL)
    }
}