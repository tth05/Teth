package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.lang.lexer.TokenStream
import com.github.tth05.teth.lang.lexer.TokenizerResult
import com.github.tth05.teth.lang.parser.ASTVisitor
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.parser.SourceFileUnit
import com.github.tth05.teth.lang.parser.ast.*
import com.github.tth05.teth.lang.source.InMemorySource
import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.tree.IElementType

/**
 * Simple parser which turns the given tokens into a flat AST
 */
class TethParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        if (builder !is PsiBuilderImpl)
            throw IllegalArgumentException("PsiBuilder must be an instance of PsiBuilderImpl")

        val tokenizerResult = (builder.lexer as TethLexer).tokenizerResult!!
        val parserResult = Parser.parse(
            // We create another result here because the lexer had no module name information
            TokenizerResult(
                TokenStream(InMemorySource("test", ""), tokenizerResult.tokenStream.tokens),
                tokenizerResult.problems
            )
        )

        val rootMarker = builder.mark()

        PsiConstructorVisitor(builder).visit(parserResult.unit)

        rootMarker.done(root)

        return builder.treeBuilt
    }
}

private class PsiConstructorVisitor(val builder: PsiBuilder) : ASTVisitor() {

    override fun visit(unit: SourceFileUnit?) {
        val marker = builder.mark()
        super.visit(unit)
        marker.done(TethElementTypes.UNIT)
    }

    override fun visit(declaration: VariableDeclaration) {
        marked(declaration, TethElementTypes.VARIABLE_DECLARATION) {
            super.visit(declaration)
        }
    }

    override fun visit(expression: BinaryExpression) {
        marked(expression, TethElementTypes.BINARY_EXPRESSION) {
            super.visit(expression)
        }
    }

    override fun visit(typeExpression: TypeExpression) {
        marked(typeExpression, TethElementTypes.TYPE_EXPRESSION) {
            super.visit(typeExpression)
        }
    }

    override fun visit(identifierExpression: IdentifierExpression) {
        marked(identifierExpression, TethElementTypes.IDENTIFIER_LITERAL) {
            super.visit(identifierExpression)
        }
    }

    override fun visit(longLiteralExpression: LongLiteralExpression) {
        marked(longLiteralExpression, TethElementTypes.LONG_LITERAL) {
            super.visit(longLiteralExpression)
        }
    }

    fun marked(statement: Statement, type: IElementType, block: () -> Unit) {
        advanceFloatingTokens(statement.span.offset)
        val marker = builder.mark()
        block()
        advanceFloatingTokens(statement.span.offsetEnd)
        marker.done(type)
    }

    private fun advanceFloatingTokens(offset: Int) {
        while (builder.currentOffset < offset) {
            val tokenMarker = builder.mark()
            val tokenType = builder.tokenType!!
            builder.advanceLexer()
            tokenMarker.done(tokenType)
        }
    }
}
