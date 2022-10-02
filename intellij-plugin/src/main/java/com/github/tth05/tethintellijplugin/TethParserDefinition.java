package com.github.tth05.tethintellijplugin;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PlainTextTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

public class TethParserDefinition implements ParserDefinition {

    private static final IFileElementType TETH_FILE_ELEMENT_TYPE = new IFileElementType(TethLanguage.INSTANCE) {
        @Override
        public ASTNode parseContents(@NotNull ASTNode chameleon) {
            final CharSequence chars = chameleon.getChars();
            return ASTFactory.leaf(PlainTextTokenTypes.PLAIN_TEXT, chars);
        }
    };

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new EmptyLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return TETH_FILE_ELEMENT_TYPE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return PsiUtilCore.NULL_PSI_ELEMENT;
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new TethPsiFileImpl(viewProvider);
    }
}
