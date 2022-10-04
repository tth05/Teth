package com.github.tth05.tethintellijplugin

import com.github.tth05.tethintellijplugin.syntax.highlighting.TethSyntaxHighlighter
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class TethColorSettingsPage : ColorSettingsPage {
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Teth"

    override fun getIcon(): Icon = TethIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = TethSyntaxHighlighter()

    override fun getDemoText(): String = """
        fn demo() {}
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Separator", TethSyntaxHighlighter.SEPARATOR),
            AttributesDescriptor("Operator", TethSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Identifier", TethSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Parameter", TethSyntaxHighlighter.PARAMETER),
            AttributesDescriptor("Keyword", TethSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("String literal", TethSyntaxHighlighter.STRING_VALUE),
            AttributesDescriptor("Number literal", TethSyntaxHighlighter.NUMBER_VALUE),
            AttributesDescriptor("Comment", TethSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Function call", TethSyntaxHighlighter.FUNCTION_CALL),
            AttributesDescriptor("Type", TethSyntaxHighlighter.TYPE),
            AttributesDescriptor("Type parameter", TethSyntaxHighlighter.TYPE_PARAMETER),
            AttributesDescriptor("Field", TethSyntaxHighlighter.FIELD),
            AttributesDescriptor("Bad value", TethSyntaxHighlighter.BAD_CHARACTER)
        )
    }
}