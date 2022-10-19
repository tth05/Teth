package com.github.tth05.tethintellijplugin.completion

import com.github.tth05.teth.analyzer.completion.AutoCompletion
import com.github.tth05.teth.analyzer.completion.CompletionItem
import com.github.tth05.tethintellijplugin.psi.caching.tethCache
import com.github.tth05.tethintellijplugin.syntax.analyzeAndParseFile
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class TethCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
                ) {
                    val (analyzer, _, parserResult) = parameters.originalFile.tethCache()
                        .resolveWithCaching(parameters.originalFile) { analyzeAndParseFile(it) }

                    val autoCompletion = AutoCompletion(analyzer, parserResult.unit)
                    autoCompletion.complete(parameters.offset).forEach {
                        result.addElement(it.toLookupElement())
                    }
                }
            }
        )
    }

    private fun CompletionItem.toLookupElement(): LookupElement = PrioritizedLookupElement.withPriority(
        LookupElementBuilder.create(text)
            .withIcon(
                when (type!!) {
                    CompletionItem.Type.FUNCTION -> AllIcons.Nodes.Function
                    CompletionItem.Type.STRUCT -> AllIcons.Nodes.Class
                    CompletionItem.Type.FIELD -> AllIcons.Nodes.Field
                    CompletionItem.Type.VARIABLE -> AllIcons.Nodes.Variable
                    CompletionItem.Type.PARAMETER -> AllIcons.Nodes.Parameter
                }
            )
            .withTailText(tailText, true)
            .withTypeText(typeText, true)
            .withInsertHandler { context, item ->
                if (type != CompletionItem.Type.FUNCTION)
                    return@withInsertHandler

                val offset = context.tailOffset
                context.document.insertString(offset, "()")
                context.editor.caretModel.moveToOffset(offset + if (this.tailText.length <= 2 /* No parameters */) 2 else 1)
            },
        priority.toDouble()
    )
}