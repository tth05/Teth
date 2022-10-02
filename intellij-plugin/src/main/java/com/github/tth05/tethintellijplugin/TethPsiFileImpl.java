package com.github.tth05.tethintellijplugin;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class TethPsiFileImpl extends PsiFileBase {

    public TethPsiFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, TethLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TethLanguageFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Teth PSI File";
    }
}
