package com.github.tth05.tethintellijplugin.syntax.psi

import com.github.tth05.tethintellijplugin.TethLanguage
import com.github.tth05.tethintellijplugin.TethLanguageFileType
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class TethPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TethLanguage.INSTANCE) {
    override fun getFileType(): FileType = TethLanguageFileType.INSTANCE

    override fun toString(): String = "Teth File"
}