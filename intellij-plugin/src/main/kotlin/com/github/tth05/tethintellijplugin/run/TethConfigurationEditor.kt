package com.github.tth05.tethintellijplugin.run

import com.github.tth05.tethintellijplugin.TethLanguageFileType
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.text.nullize
import javax.swing.JComponent

class TethConfigurationEditor(project: Project) : SettingsEditor<TethRunConfiguration>() {
    private val filePath = TextFieldWithBrowseButton(null, this).also {
        it.addBrowseFolderListener(
            "File Path",
            "Path to the entry point file",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor(TethLanguageFileType.INSTANCE),
        )
    }

    override fun resetEditorFrom(c: TethRunConfiguration) {
        filePath.text = c.filePath?.let(FileUtil::toSystemDependentName).orEmpty()
    }

    override fun applyEditorTo(c: TethRunConfiguration) {
        c.filePath = filePath.text.nullize()?.let(FileUtil::toSystemIndependentName)
    }

    override fun createEditor(): JComponent = panel {
        row("File Path") {
            cell(filePath).horizontalAlign(HorizontalAlign.FILL)
        }
    }
}