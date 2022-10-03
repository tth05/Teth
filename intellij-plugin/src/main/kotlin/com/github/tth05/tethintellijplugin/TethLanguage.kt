package com.github.tth05.tethintellijplugin

import com.github.tth05.tethintellijplugin.TethLanguage
import com.intellij.lang.Language

class TethLanguage : Language("Teth") {
    companion object {
        val INSTANCE: TethLanguage = TethLanguage()
    }
}