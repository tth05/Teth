package com.github.tth05.tethintellijplugin.run

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class TethRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element !is TethPsiFile)
            return null

        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            { el -> "Run file" },
            *ExecutorAction.getActions(0)
        )
    }
}