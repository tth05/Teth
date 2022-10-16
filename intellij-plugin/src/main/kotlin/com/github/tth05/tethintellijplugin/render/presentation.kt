package com.github.tth05.tethintellijplugin.render

import com.github.tth05.teth.lang.parser.ast.VariableDeclaration
import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.github.tth05.tethintellijplugin.psi.api.*
import com.github.tth05.tethintellijplugin.psi.impl.TethNamedElement
import com.github.tth05.tethintellijplugin.syntax.highlighting.TethSyntaxHighlighter
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.documentation.DocumentationSettings
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import javax.swing.Icon

fun getPresentation(element: PsiElement): ItemPresentation? {
    if (element !is TethNamedElement)
        return null

    return PresentationData(
        element.name,
        element.containingFile.virtualFile.toNioPath().toString(),
        getIcon(element),
        null
    )
}

fun getIcon(element: PsiElement): Icon? {
    return when (element) {
        is TethStructDeclaration -> AllIcons.Nodes.Class
        is TethFieldDeclaration -> AllIcons.Nodes.Field
        is TethFunctionDeclaration -> AllIcons.Nodes.Method
        is TethFunctionParameterDeclaration -> AllIcons.Nodes.Parameter
        is TethVariableDeclaration -> AllIcons.Nodes.Variable
        is TethGenericParameterDeclaration -> AllIcons.Nodes.Type
        else -> null
    }
}

fun renderElement(element: PsiElement): String {
    val builder = HTMLRenderBuilder
    builder.clear()
    return renderElement(element, builder)
}

private fun renderElement(element: PsiElement, builder: RenderBuilder): String {
    when (element) {
        is TethPsiFile -> {
            builder.append(element.name)
        }

        is TethStructDeclaration -> {
            builder.appendStyled("struct ", TethSyntaxHighlighter.KEYWORD)
                .appendStyled(element.name.orEmpty(), TethSyntaxHighlighter.TYPE)
            renderGenericParameters(element.genericParameters, builder)
        }

        is TethFieldDeclaration -> {
            builder.appendStyled(element.name.orEmpty(), TethSyntaxHighlighter.FIELD)
                .appendStyled(": ", TethSyntaxHighlighter.SEPARATOR)
            renderElement(element.type, builder)
        }

        is TethFunctionDeclaration -> {
            builder.appendStyled("fn ", TethSyntaxHighlighter.KEYWORD)
                .appendStyled(element.name.orEmpty(), TethSyntaxHighlighter.FUNCTION_CALL)
            renderGenericParameters(element.genericParameters, builder)
            builder.appendStyled("(", TethSyntaxHighlighter.SEPARATOR)
            renderCommaList(element.parameters, builder)
            builder.appendStyled(")", TethSyntaxHighlighter.SEPARATOR)

            if (element.returnType != null)
                renderElement(element.returnType!!, builder)
        }

        is TethFunctionParameterDeclaration -> {
            builder.appendStyled(element.name.orEmpty(), TethSyntaxHighlighter.PARAMETER)
                .appendStyled(": ", TethSyntaxHighlighter.SEPARATOR)
            renderElement(element.type, builder)
        }

        is TethVariableDeclaration -> {
            builder.appendStyled("let ", TethSyntaxHighlighter.KEYWORD)
                .appendStyled(element.name.orEmpty(), TethSyntaxHighlighter.IDENTIFIER)

            if (element.type != null) {
                builder.appendStyled(": ", TethSyntaxHighlighter.SEPARATOR)
                renderElement(element.type!!, builder)
            }
        }

        is TethGenericParameterDeclaration -> {
            builder.appendStyled(element.name.orEmpty(), TethSyntaxHighlighter.TYPE_PARAMETER)
        }

        is TethTypeExpression -> {
            renderType(element, builder)
        }
    }

    return builder.toString()
}

private fun renderCommaList(list: List<PsiElement>, builder: RenderBuilder) {
    list.forEach {
        renderElement(it, builder)

        if (it != list.last())
            builder.append(", ")
    }
}

private fun renderType(type: TethTypeExpression, builder: RenderBuilder) {
    builder.appendStyled(type.typeName.identifier.orEmpty(), TethSyntaxHighlighter.TYPE)
    if (type.genericArguments.isNotEmpty()) {
        builder.appendStyled("<", TethSyntaxHighlighter.OPERATOR)
        renderCommaList(type.genericArguments, builder)
        builder.appendStyled(">", TethSyntaxHighlighter.OPERATOR)
    }
}

private fun renderGenericParameters(genericParameters: List<TethGenericParameterDeclaration>, builder: RenderBuilder) {
    if (genericParameters.isEmpty())
        return

    builder.appendStyled("<", TethSyntaxHighlighter.OPERATOR)
    renderCommaList(genericParameters, builder)
    builder.appendStyled(">", TethSyntaxHighlighter.OPERATOR)
}

private abstract class RenderBuilder {
    protected val builder: StringBuilder = StringBuilder()

    abstract fun appendStyled(text: String, style: TextAttributesKey): RenderBuilder

    open fun append(text: String): RenderBuilder = apply { builder.append(text) }
    fun clear() = apply { builder.clear() }

    override fun toString(): String = builder.toString()
}

private object PlainRenderBuilder : RenderBuilder() {
    override fun appendStyled(text: String, style: TextAttributesKey): RenderBuilder = apply { append(text) }
}

private object HTMLRenderBuilder : RenderBuilder() {
    override fun append(text: String): RenderBuilder = super.append(StringUtil.escapeXmlEntities(text))

    override fun appendStyled(text: String, style: TextAttributesKey) = apply {
        HtmlSyntaxInfoUtil.appendStyledSpan(
            builder,
            style,
            StringUtil.escapeXmlEntities(text),
            DocumentationSettings.getHighlightingSaturation(false),
        )
    }
}