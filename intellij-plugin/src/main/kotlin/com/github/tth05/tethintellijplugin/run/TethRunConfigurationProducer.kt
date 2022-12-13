package com.github.tth05.tethintellijplugin.run

import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import kotlin.io.path.absolutePathString

class TethRunConfigurationProducer : LazyRunConfigurationProducer<TethRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        TethConfigurationType.getInstance().configurationFactories.single()

    override fun setupConfigurationFromContext(
        configuration: TethRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.psiLocation?.containingFile ?: return false
        if (file !is TethPsiFile) return false

        configuration.filePath = file.virtualFile?.toNioPath()?.absolutePathString() ?: return false
        sourceElement.set(file)
        return true
    }

    override fun isConfigurationFromContext(
        configuration: TethRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.psiLocation?.containingFile ?: return false
        return configuration.filePath == file.virtualFile?.toNioPath()?.absolutePathString()
    }
}