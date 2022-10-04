package com.github.tth05.tethintellijplugin

import com.github.tth05.tethintellijplugin.TethLanguage
import com.intellij.lang.Language

class TethLanguage : Language("Teth") {

    override fun isCaseSensitive(): Boolean = true

    companion object {
        val INSTANCE: TethLanguage = TethLanguage()
    }
}