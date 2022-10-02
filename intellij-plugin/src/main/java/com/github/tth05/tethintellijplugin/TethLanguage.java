package com.github.tth05.tethintellijplugin;

import com.intellij.lang.Language;

public class TethLanguage extends Language {

    public static final TethLanguage INSTANCE = new TethLanguage();

    private TethLanguage() {
        super("Teth");
    }
}
