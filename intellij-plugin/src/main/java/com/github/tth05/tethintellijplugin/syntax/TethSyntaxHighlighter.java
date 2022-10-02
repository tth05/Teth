package com.github.tth05.tethintellijplugin.syntax;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class TethSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey SEPARATOR = createTextAttributesKey("TETH_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("TETH_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("TETH_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING_VALUE = createTextAttributesKey("TETH_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER_VALUE = createTextAttributesKey("TETH_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("TETH_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("TETH_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TethLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType == TethTokenTypes.SEPARATOR)
            return pack(SEPARATOR);
        if (tokenType == TethTokenTypes.IDENTIFIER)
            return pack(IDENTIFIER);
        if (tokenType == TethTokenTypes.KEYWORD)
            return pack(KEYWORD);
        if (tokenType == TethTokenTypes.STRING_LITERAL)
            return pack(STRING_VALUE);
        if (tokenType == TethTokenTypes.DOUBLE || tokenType == TethTokenTypes.LONG)
            return pack(NUMBER_VALUE);
        if (tokenType == TethTokenTypes.COMMENT)
            return pack(COMMENT);
        if (tokenType == TokenType.BAD_CHARACTER)
            return pack(BAD_CHARACTER);
        return pack(null);
    }
}
