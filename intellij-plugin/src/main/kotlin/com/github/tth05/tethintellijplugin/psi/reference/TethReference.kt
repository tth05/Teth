package com.github.tth05.tethintellijplugin.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

abstract class TethReference<T : PsiElement>(element: T) : PsiReferenceBase<T>(element) {

    override fun calculateDefaultRangeInElement(): TextRange {
        return TextRange.from(0, element.textLength)
    }
}

inline fun <reified T : PsiElement> T.resolvedRefTo(target: PsiElement?) =
    object : TethReference<T>(this) {
        override fun resolve(): PsiElement? = target
    }
