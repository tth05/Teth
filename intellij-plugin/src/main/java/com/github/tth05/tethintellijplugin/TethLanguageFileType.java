package com.github.tth05.tethintellijplugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TethLanguageFileType extends LanguageFileType {

    public static final TethLanguageFileType INSTANCE = new TethLanguageFileType();

    private TethLanguageFileType() {
        super(TethLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Teth file";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Teth language file";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "teth";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Debugger.AddToWatch;
    }
}
