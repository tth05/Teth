package com.github.tth05.tethintellijplugin.psi.reference

import com.github.tth05.tethintellijplugin.psi.api.TethIdentifierLiteralExpression
import com.github.tth05.tethintellijplugin.psi.impl.FakeElementFactory
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

abstract class TethReference<T : PsiElement>(element: T) : PsiReferenceBase<T>(element) {
    override fun calculateDefaultRangeInElement(): TextRange {
        return TextRange.from(0, element.textLength)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        if (element is TethIdentifierLiteralExpression) {
            val expression = FakeElementFactory(element.project).createIdentifierLiteralExpression(newElementName)
            element.replace(expression)
            return expression
        }

        return super.handleElementRename(newElementName)
    }

    companion object {
        val UNRESOLVED = object : TethReference<PsiElement>(ASTWrapperPsiElement(ASTFactory.whitespace(""))) {
            override fun calculateDefaultRangeInElement(): TextRange = throw NotImplementedError()
            override fun resolve(): PsiElement = throw NotImplementedError()
        }
    }
}

fun <T : PsiElement> T.resolvedRefTo(target: PsiElement?) =
    object : TethReference<T>(this@resolvedRefTo) {
        override fun resolve(): PsiElement? = target
    }
