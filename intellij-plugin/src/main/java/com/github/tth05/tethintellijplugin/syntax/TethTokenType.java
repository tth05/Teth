package com.github.tth05.tethintellijplugin.syntax;

import com.github.tth05.tethintellijplugin.TethLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TethTokenType extends IElementType {

    public TethTokenType(@NonNls @NotNull String debugName) {
        super(debugName, TethLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "TethTokenType." + super.toString();
    }
}
