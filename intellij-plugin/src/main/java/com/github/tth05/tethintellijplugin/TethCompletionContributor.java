package com.github.tth05.tethintellijplugin;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

public class TethCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        result.addElement(LookupElementBuilder.create("Yeeti").withIcon(AllIcons.General.OverridenMethod).withBoldness(true).withTypeText("Type text").appendTailText("tail", true));
    }
}
