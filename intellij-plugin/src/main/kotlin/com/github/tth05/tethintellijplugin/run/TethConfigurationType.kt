package com.github.tth05.tethintellijplugin.run

import com.github.tth05.tethintellijplugin.TethIcons
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil

class TethConfigurationType : ConfigurationTypeBase(
    "TethRunConfiguration",
    "Teth",
    "Teth run configuration",
    TethIcons.FILE
) {
    init {
        addFactory(TethConfigurationFactory(this))
    }

    companion object {
        fun getInstance() =  ConfigurationTypeUtil.findConfigurationType(TethConfigurationType::class.java)
    }
}