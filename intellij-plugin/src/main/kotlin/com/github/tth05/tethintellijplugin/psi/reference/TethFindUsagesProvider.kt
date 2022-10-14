package com.github.tth05.tethintellijplugin.psi.reference

import com.github.tth05.tethintellijplugin.psi.api.*
import com.intellij.lang.HelpID
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement

class TethFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is TethNameIdentifierOwner && !psiElement.name.isNullOrEmpty()

    override fun getHelpId(psiElement: PsiElement): String = HelpID.FIND_OTHER_USAGES

    override fun getType(element: PsiElement): String = when (element) {
        is TethStructDeclaration -> "Struct"
        is TethFieldDeclaration -> "Field"
        is TethFunctionDeclaration -> "Function"
        is TethFunctionParameterDeclaration -> "Parameter"
        is TethVariableDeclaration -> "Variable"
        is TethGenericParameterDeclaration -> "Generic parameter"
        else -> "TODO: FindUsagesProvider#getType for ${element.javaClass.name}"
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return (element as TethNameIdentifierOwner).name
            ?: "Error: FindUsagesProvider#getDescriptiveName for ${element.javaClass.name}"
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return "node text"
    }
}