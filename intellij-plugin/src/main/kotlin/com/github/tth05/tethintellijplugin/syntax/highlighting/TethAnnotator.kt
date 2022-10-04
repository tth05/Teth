package com.github.tth05.tethintellijplugin.syntax.highlighting

import com.github.tth05.teth.analyzer.Analyzer
import com.github.tth05.teth.lang.diagnostics.Problem
import com.github.tth05.teth.lang.parser.ASTVisitor
import com.github.tth05.teth.lang.parser.Parser
import com.github.tth05.teth.lang.parser.ast.*
import com.github.tth05.teth.lang.parser.ast.FunctionDeclaration.ParameterDeclaration
import com.github.tth05.teth.lang.parser.ast.StructDeclaration.FieldDeclaration
import com.github.tth05.teth.lang.source.InMemorySource
import com.github.tth05.teth.lang.span.Span
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class TethAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiFile) return

        val t = System.nanoTime()
        val parserResult = Parser.parse(InMemorySource("", element.text))
        val validTextRange = element.textRange
        parserResult.problems.forEach {
            annotateError(holder, validTextRange, it)
        }

        val analyzer = Analyzer(mutableListOf(parserResult.unit))
        val analyzerResults = analyzer.analyze()
        analyzerResults.filter { it.moduleName == parserResult.unit.moduleName }.flatMap { it.problems }.forEach {
            annotateError(holder, validTextRange, it)
        }

        AnnotatingVisitor(analyzer, holder).visit(parserResult.unit)

        println("Annotating took ${(System.nanoTime() - t) / 1_000_000.0}ms")
    }

    private fun annotateError(
        holder: AnnotationHolder, validTextRange: TextRange, it: Problem
    ) {
        var range = it.span.toTextRange()
        if (!validTextRange.contains(range)) {
            range = if (validTextRange.endOffset - 1 >= 0) TextRange(
                validTextRange.endOffset - 1,
                validTextRange.endOffset
            ) else return
        }

        holder.newAnnotation(HighlightSeverity.ERROR, it.message).range(range).needsUpdateOnTyping()
            .create()
    }
}

private class AnnotatingVisitor(val analyzer: Analyzer, val holder: AnnotationHolder) : ASTVisitor() {
    override fun visit(declaration: StructDeclaration?) {
        super.visit(declaration)

        if (declaration!!.nameExpr.value == null) return

        annotateWithColor(declaration.nameExpr.span, TethSyntaxHighlighter.TYPE)
    }

    override fun visit(declaration: FunctionDeclaration?) {
        super.visit(declaration)

        if (declaration!!.nameExpr.value == null) return

        annotateWithColor(declaration.nameExpr.span, TethSyntaxHighlighter.FUNCTION_CALL)
    }

    override fun visit(declaration: GenericParameterDeclaration?) {
        super.visit(declaration)

        if (declaration!!.nameExpr.value == null) return

        annotateWithColor(declaration.nameExpr.span, TethSyntaxHighlighter.TYPE)
    }

    override fun visit(declaration: FieldDeclaration?) {
        super.visit(declaration)

        if (declaration!!.nameExpr.value == null) return

        annotateWithColor(declaration.nameExpr.span, TethSyntaxHighlighter.FIELD)
    }

    override fun visit(parameter: ParameterDeclaration?) {
        super.visit(parameter)

        if (parameter!!.nameExpr.value == null) return

        annotateWithColor(parameter.nameExpr.span, TethSyntaxHighlighter.PARAMETER)
    }

    override fun visit(identifierExpression: IdentifierExpression?) {
        when (analyzer.resolvedReference(identifierExpression!!)) {
            is FunctionDeclaration -> annotateWithColor(
                identifierExpression.span, TethSyntaxHighlighter.FUNCTION_CALL
            )

            is StructDeclaration, is GenericParameterDeclaration -> annotateWithColor(
                identifierExpression.span, TethSyntaxHighlighter.TYPE
            )

            is ParameterDeclaration -> annotateWithColor(
                identifierExpression.span, TethSyntaxHighlighter.PARAMETER
            )

            is FieldDeclaration -> annotateWithColor(
                identifierExpression.span, TethSyntaxHighlighter.FIELD
            )
        }
    }

    override fun visit(typeExpression: TypeExpression?) {
        super.visit(typeExpression)

        when (analyzer.resolvedReference(typeExpression!!)) {
            is StructDeclaration, is GenericParameterDeclaration -> annotateWithColor(
                Span(
                    typeExpression.span.source,
                    typeExpression.span.offset,
                    typeExpression.span.offset + typeExpression.name.length
                ),
                TethSyntaxHighlighter.TYPE
            )
        }
    }

    private fun annotateWithColor(span: Span, color: TextAttributesKey) {
        holder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "").range(span.toTextRange())
            .textAttributes(color).needsUpdateOnTyping().create()
    }
}

private fun Span.toTextRange() = TextRange.from(this.offset, this.length)