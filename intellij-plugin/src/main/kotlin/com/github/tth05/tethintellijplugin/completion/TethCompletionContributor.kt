package com.github.tth05.tethintellijplugin.completion

import com.github.tth05.teth.analyzer.completion.AutoCompletion
import com.github.tth05.tethintellijplugin.psi.caching.tethCache
import com.github.tth05.tethintellijplugin.syntax.analyzeAndParseFile
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.jetbrains.rd.util.string.printToString

class TethCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
                ) {
                    val (analyzer, _, parserResult) = parameters.originalFile.tethCache()
                        .resolveWithCaching(parameters.originalFile) { analyzeAndParseFile(it) }

                    val autoCompletion = AutoCompletion(analyzer, parserResult.unit)
                    autoCompletion.complete(parameters.offset).forEach {
                        result.addElement(LookupElementBuilder.create(it.text).withIcon(AllIcons.Nodes.Method))
                    }
                }
            })
    }
}