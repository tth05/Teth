package com.github.tth05.tethintellijplugin.syntax.brace

import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.github.tth05.tethintellijplugin.syntax.TethTokenTypes
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet

class TethBraceMatchingTypedHandler : TypedHandlerDelegate() {

    private var ltTyped = false

    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        if (file !is TethPsiFile) return Result.CONTINUE

        ltTyped = c == '<' && TypedHandlerUtil.isAfterClassLikeIdentifierOrDot(
            editor.caretModel.offset,
            editor,
            TethTokenTypes.IDENTIFIER,
            TethTokenTypes.IDENTIFIER,
            true
        ) && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET

        if (c == '>' && TypedHandlerUtil.handleGenericGT(
                editor,
                TethTokenTypes.LESS,
                TethTokenTypes.GREATER,
                INVALID_INSIDE_REFERENCE
            ) && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET
        ) return Result.STOP

        return Result.CONTINUE
    }

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is TethPsiFile || !ltTyped) return Result.CONTINUE

        ltTyped = false
        TypedHandlerUtil.handleAfterGenericLT(
            editor,
            TethTokenTypes.LESS,
            TethTokenTypes.GREATER,
            INVALID_INSIDE_REFERENCE
        )

        return Result.STOP
    }

    companion object {
        val INVALID_INSIDE_REFERENCE = TokenSet.create(
            TethTokenTypes.L_PAREN,
            TethTokenTypes.R_PAREN,
            TethTokenTypes.L_CURLY_PAREN,
            TethTokenTypes.R_CURLY_PAREN
        )
    }
}