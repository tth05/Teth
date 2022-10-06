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
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
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

        val d = (System.nanoTime() - t) / 1_000_000.0
        println("Annotating 1 took ${d}ms")
        AnnotatingVisitor(analyzer, holder).visit(parserResult.unit)
        val d2 = (System.nanoTime() - t) / 1_000_000.0
        println("Annotating took ${d2}ms")
    }

    private fun annotateError(
        holder: AnnotationHolder, validTextRange: TextRange, it: Problem
    ) {
        var range = it.span.toTextRange()
        // Fixes errors at EOF
        if (!validTextRange.contains(range)) {
            range = if (validTextRange.endOffset - 1 >= 0) TextRange(
                validTextRange.endOffset - 1,
                validTextRange.endOffset
            ) else return
        }

        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, it.message).highlightType(ProblemHighlightType.GENERIC_ERROR).range(range)
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

        annotateWithColor(declaration.nameExpr.span, TethSyntaxHighlighter.TYPE_PARAMETER)
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

            is StructDeclaration -> annotateWithColor(
                identifierExpression.span, TethSyntaxHighlighter.TYPE
            )

            is GenericParameterDeclaration -> annotateWithColor(
                identifierExpression.span, TethSyntaxHighlighter.TYPE_PARAMETER
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

        if (typeExpression!!.name == null)
            return

        val span = Span(
            typeExpression.span.source,
            typeExpression.span.offset,
            typeExpression.span.offset + typeExpression.name.length
        )

        when (analyzer.resolvedReference(typeExpression)) {
            is StructDeclaration -> annotateWithColor(span, TethSyntaxHighlighter.TYPE)
            is GenericParameterDeclaration -> annotateWithColor(span, TethSyntaxHighlighter.TYPE_PARAMETER)
        }
    }

    private fun annotateWithColor(span: Span, color: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES).range(span.toTextRange())
            .textAttributes(color).needsUpdateOnTyping().create()
    }
}

private fun Span.toTextRange() = TextRange.from(this.offset, this.length)