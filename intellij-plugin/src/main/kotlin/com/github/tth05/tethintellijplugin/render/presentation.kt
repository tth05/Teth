package com.github.tth05.tethintellijplugin.render

import com.github.tth05.tethintellijplugin.psi.api.*
import com.github.tth05.tethintellijplugin.psi.impl.TethNamedElement
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

fun getPresentation(element: PsiElement): ItemPresentation? {
    if (element !is TethNamedElement)
        return null

    return PresentationData(
        element.name,
        element.containingFile.virtualFile.toNioPath().toString(),
        element.getIcon(0),
        null
    )
}

fun getIcon(element: PsiElement): Icon? {
    return when (element) {
        is TethStructDeclaration -> AllIcons.Nodes.Class
        is TethFieldDeclaration -> AllIcons.Nodes.Field
        is TethFunctionDeclaration -> AllIcons.Nodes.Method
        is TethFunctionParameterDeclaration -> AllIcons.Nodes.Parameter
        else -> null
    }
}