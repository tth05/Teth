package com.github.tth05.tethintellijplugin.codeInsight

import com.github.tth05.teth.analyzer.type.SemanticType
import com.github.tth05.teth.lang.parser.ASTUtil
import com.github.tth05.teth.lang.parser.ast.VariableDeclaration
import com.github.tth05.tethintellijplugin.psi.api.TethVariableDeclaration
import com.github.tth05.tethintellijplugin.psi.caching.tethCache
import com.github.tth05.tethintellijplugin.syntax.analyzeAndParseFile
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import javax.swing.JComponent
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class TethInlayHintsProvider : InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings>
        get() = KEY
    override val name: String = "TethInlayHintsProvider"
    override val previewText: String? = null

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector = object : FactoryInlayHintsCollector(editor) {
        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if (element !is TethVariableDeclaration || element.type != null || element.name == null)
                return true

            val (analyzer, _, result) = element.tethCache().resolveWithCaching(file) {
                analyzeAndParseFile(file)
            }

            val variableDeclaration = ASTUtil.findStatementAtExact(result.unit, element.letKeyword.startOffset)
                ?.let { it as? VariableDeclaration }
                ?: return true

            val resolvedType = analyzer.resolvedExpressionType(variableDeclaration.initializerExpr)
                .takeIf { it != SemanticType.UNRESOLVED } ?: return false

            sink.addInlineElement(
                element.nameIdentifier!!.endOffset,
                false,
                factory.roundWithBackgroundAndSmallInset(factory.text(": ${analyzer.typeCache.toString(resolvedType)}")),
                false
            )
            return true
        }
    }

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener): JComponent = JPanel()
    }

    companion object {
        val KEY = SettingsKey<NoSettings>("teth.hints.provider")
    }
}