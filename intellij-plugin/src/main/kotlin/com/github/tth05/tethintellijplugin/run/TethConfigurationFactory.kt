package com.github.tth05.tethintellijplugin.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class TethConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String = "Teth run configuration"

    override fun createTemplateConfiguration(project: Project): RunConfiguration = TethRunConfiguration(project, this)
}