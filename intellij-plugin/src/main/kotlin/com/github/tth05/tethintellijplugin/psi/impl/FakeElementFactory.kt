package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.TethLanguageFileType
import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.github.tth05.tethintellijplugin.psi.api.TethIdentifierLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.descendantsOfType

class FakeElementFactory(private val project: Project) {

    private fun createFile(text: String, fileName: String = "dummy.teth") = PsiFileFactory
        .getInstance(project)
        .createFileFromText(
            fileName,
            TethLanguageFileType.INSTANCE,
            text,
        ) as TethPsiFile

    fun createIdentifierLiteralExpression(text: String): TethIdentifierLiteralExpression {
        return createFile(text).descendantsOfType<TethIdentifierLiteralExpression>().firstOrNull()
            ?: error("Could not create identifier literal expression")
    }
}