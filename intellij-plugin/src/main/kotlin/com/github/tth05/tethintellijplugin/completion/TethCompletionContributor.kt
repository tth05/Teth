package com.github.tth05.tethintellijplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

class TethCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
                ) {
                    val originalElement = CompletionUtil.getOriginalOrSelf<PsiElement>(parameters.position)
                    val actualText = ElementManipulators.getValueText(originalElement)
                    if (actualText.isBlank()) return

                    result.addElement(LookupElementBuilder.create(actualText + Math.random()))
                    result.addElement(LookupElementBuilder.create(actualText + Math.random()))
                    result.addElement(LookupElementBuilder.create(actualText + Math.random()))
                }
            })
    }
}