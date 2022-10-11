package com.github.tth05.tethintellijplugin.syntax

import com.github.tth05.teth.lang.lexer.TokenStream
import com.github.tth05.teth.lang.lexer.TokenizerResult
import com.github.tth05.teth.lang.parser.ASTVisitor
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.parser.SourceFileUnit
import com.github.tth05.teth.lang.parser.ast.*
import com.github.tth05.teth.lang.source.InMemorySource
import com.github.tth05.teth.lang.span.Span
import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.IElementType

class TethParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        if (builder !is PsiBuilderImpl)
            throw IllegalArgumentException("PsiBuilder must be an instance of PsiBuilderImpl")

        val tokenizerResult = (builder.lexer as? TethLexer)?.tokenizerResult
        if (tokenizerResult == null) {
            val rootMarker = builder.mark()
            rootMarker.done(root)
            return builder.treeBuilt
        }

        val file =
            builder.getUserData(FileContextUtil.CONTAINING_FILE_KEY)?.originalFile
                ?: throw IllegalStateException("No file available")

        val parserResult = Parser.parse(
            // We create another result here because the lexer had no module name information
            TokenizerResult(
                TokenStream(InMemorySource(file.name.removeSuffix(".teth"), ""), tokenizerResult.tokenStream.tokens),
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
        marked(Span(null, 0, builder.originalText.length), TethElementTypes.UNIT) {
            super.visit(unit)
        }
    }

    override fun visit(declaration: StructDeclaration) {
        marked(declaration, TethElementTypes.STRUCT_DECLARATION) {
            declaration.nameExpr.accept(this)
            declaration.genericParameters.forEach { it.accept(this) }

            (declaration.fields.asSequence() + declaration.functions.asSequence())
                .sortedBy { it.span.offset }
                .forEach { it.accept(this) }
        }
    }

    override fun visit(declaration: StructDeclaration.FieldDeclaration) {
        marked(declaration, TethElementTypes.FIELD_DECLARATION) {
            super.visit(declaration)
        }
    }

    override fun visit(declaration: FunctionDeclaration) {
        marked(declaration, TethElementTypes.FUNCTION_DECLARATION) {
            super.visit(declaration)
        }
    }

    override fun visit(declaration: FunctionDeclaration.ParameterDeclaration) {
        marked(declaration, TethElementTypes.FUNCTION_PARAMETER_DECLARATION) {
            super.visit(declaration)
        }
    }

    override fun visit(declaration: GenericParameterDeclaration) {
        marked(declaration, TethElementTypes.GENERIC_PARAMETER_DECLARATION) {
            super.visit(declaration)
        }
    }

    override fun visit(declaration: VariableDeclaration) {
        marked(declaration, TethElementTypes.VARIABLE_DECLARATION) {
            super.visit(declaration)
        }
    }

    override fun visit(statement: BlockStatement) {
        marked(statement, TethElementTypes.BLOCK) {
            super.visit(statement)
        }
    }

    override fun visit(statement: IfStatement) {
        marked(statement, TethElementTypes.IF_STATEMENT) {
            super.visit(statement)
        }
    }

    override fun visit(statement: LoopStatement) {
        marked(statement, TethElementTypes.LOOP_STATEMENT) {
            super.visit(statement)
        }
    }

    override fun visit(returnStatement: ReturnStatement) {
        marked(returnStatement, TethElementTypes.RETURN_STATEMENT) {
            super.visit(returnStatement)
        }
    }

    override fun visit(useStatement: UseStatement) {
        marked(useStatement, TethElementTypes.USE_STATEMENT) {
            super.visit(useStatement)
        }
    }

    override fun visit(invocation: FunctionInvocationExpression) {
        marked(invocation, TethElementTypes.FUNCTION_INVOCATION) {
            super.visit(invocation)
        }
    }

    override fun visit(expression: BinaryExpression) {
        marked(expression, TethElementTypes.BINARY_EXPRESSION) {
            super.visit(expression)
        }
    }

    override fun visit(expression: UnaryExpression) {
        marked(expression, TethElementTypes.UNARY_EXPRESSION) {
            super.visit(expression)
        }
    }

    override fun visit(garbageExpression: GarbageExpression) {
        marked(garbageExpression, TethElementTypes.GARBAGE_EXPRESSION) {
            super.visit(garbageExpression)
        }
    }

    override fun visit(identifierExpression: IdentifierExpression) {
        marked(identifierExpression, TethElementTypes.IDENTIFIER_LITERAL) {
            super.visit(identifierExpression)
        }
    }

    override fun visit(expression: ObjectCreationExpression) {
        marked(expression, TethElementTypes.OBJECT_CREATION_EXPRESSION) {
            super.visit(expression)
        }
    }

    override fun visit(expression: MemberAccessExpression) {
        marked(expression, TethElementTypes.MEMBER_ACCESS_EXPRESSION) {
            super.visit(expression)
        }
    }

    override fun visit(listLiteralExpression: ListLiteralExpression) {
        marked(listLiteralExpression, TethElementTypes.LIST_LITERAL_EXPRESSION) {
            super.visit(listLiteralExpression)
        }
    }

    override fun visit(longLiteralExpression: LongLiteralExpression) {
        marked(longLiteralExpression, TethElementTypes.LONG_LITERAL) {
            super.visit(longLiteralExpression)
        }
    }

    override fun visit(doubleLiteralExpression: DoubleLiteralExpression) {
        marked(doubleLiteralExpression, TethElementTypes.DOUBLE_LITERAL) {
            super.visit(doubleLiteralExpression)
        }
    }

    override fun visit(stringLiteralExpression: StringLiteralExpression) {
        marked(stringLiteralExpression, TethElementTypes.STRING_LITERAL) {
            super.visit(stringLiteralExpression)
        }
    }

    override fun visit(booleanLiteralExpression: BooleanLiteralExpression) {
        marked(booleanLiteralExpression, TethElementTypes.BOOLEAN_LITERAL) {
            super.visit(booleanLiteralExpression)
        }
    }

    override fun visit(typeExpression: TypeExpression) {
        marked(typeExpression, TethElementTypes.TYPE) {
            super.visit(typeExpression)
        }
    }

    fun marked(statement: Statement, type: IElementType, block: () -> Unit) = marked(statement.span, type, block)

    fun marked(span: Span?, type: IElementType, block: () -> Unit) {
        advanceFloatingTokens(span?.offset ?: -1)
        val marker = builder.mark()
        block()
        advanceFloatingTokens(span?.offsetEnd ?: -1)
        marker.done(type)
    }

    private fun advanceFloatingTokens(offset: Int) {
        while (builder.currentOffset < offset && builder.tokenType != null) {
            val tokenMarker = builder.mark()
            val tokenType = builder.tokenType!!
            builder.advanceLexer()
            tokenMarker.done(tokenType)
        }
    }
}
