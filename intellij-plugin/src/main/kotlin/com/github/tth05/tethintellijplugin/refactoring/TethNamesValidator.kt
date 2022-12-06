package com.github.tth05.tethintellijplugin.refactoring

import com.github.tth05.teth.lang.lexer.Tokenizer
import com.github.tth05.teth.lang.span.Span
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

class TethNamesValidator : NamesValidator {
    override fun isKeyword(name: String, project: Project?): Boolean = Tokenizer.isKeyword(name)

    override fun isIdentifier(name: String, project: Project?): Boolean = name.matches(Regex("^[a-zA-Z0-9_]+$"))
}