package com.github.tth05.tethintellijplugin.syntax;

import com.github.tth05.teth.lang.lexer.Token;
import com.github.tth05.teth.lang.lexer.TokenType;
import com.github.tth05.teth.lang.lexer.Tokenizer;
import com.github.tth05.teth.lang.stream.CharStream;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class TethLexer extends LexerBase {

    private CharSequence buffer;
    private int bufferEnd;

    private List<Token> tokens;
    private int index;
    private Token currentToken;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.index = -1;

        if (endOffset - startOffset <= 0 || buffer.isEmpty())
            return;

        // This just tokenizes the whole file and discards any unwanted tokens. Not efficient, but fast enough
        var result = Tokenizer.tokenize(CharStream.fromChars(buffer.toString().toCharArray()));
        this.tokens = result.getTokenStream().getTokens().stream().filter(t -> t.span().offset() >= startOffset && t.span().offsetEnd() <= endOffset).collect(Collectors.toList());
        if (!this.tokens.isEmpty() && this.tokens.get(this.tokens.size() - 1).type() == TokenType.EOF)
            this.tokens.remove(this.tokens.size() - 1);

        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        if (this.currentToken == null)
            return null;

        return TethTokenTypes.of(this.currentToken.type());
    }

    @Override
    public int getTokenStart() {
        return this.currentToken.span().offset();
    }

    @Override
    public int getTokenEnd() {
        return this.currentToken.span().offsetEnd();
    }

    @Override
    public void advance() {
        this.index++;
        if (this.index >= this.tokens.size())
            this.currentToken = null;
        else
            this.currentToken = this.tokens.get(this.index);
    }

    @Override
    public int getBufferEnd() {
        return this.bufferEnd;
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return this.buffer;
    }
}
