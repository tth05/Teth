package com.github.tth05.tethintellijplugin

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class TethLanguageFileType : LanguageFileType(TethLanguage.INSTANCE) {

    override fun getName(): String = "Teth File"

    override fun getDescription(): String = "Teth language file"

    override fun getDefaultExtension(): String = "teth"

    override fun getIcon(): Icon = TethIcons.FILE

    companion object {
        val INSTANCE = TethLanguageFileType()
    }
}
