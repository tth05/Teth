package com.github.tth05.tethintellijplugin;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.tth05.tethintellijplugin.syntax.TethTypes.*;

%%

%{
  public TethLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class TethLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WHITE_SPACE=\s+
NEWLINE=[\r\n]
COMMENT=(\/\/[^\r\n]*)|(\/*.*?\*\/)
IDENTIFIER=[A-Za-z][a-zA-Z_0-9]*
NUMBER=[0-9]+
DECIMAL_NUMBER=[0-9]+\.[0-9]+

%%
<YYINITIAL> {
  {NEWLINE}          { return NEWLINE; }
  {WHITE_SPACE}      { return WHITE_SPACE; }
  {COMMENT}          { return COMMENT; }
  {DECIMAL_NUMBER}   { return DOUBLE_LITERAL; }
  {NUMBER}           { return LONG_LITERAL; }

  ","                { return COMMA; }
  "("                { return L_PAREN; }
  ")"                { return R_PAREN; }
  "{"                { return L_CURLY_PAREN; }
  "}"                { return R_CURLY_PAREN; }
  "+"                { return PLUS; }
  "-"                { return MINUS; }
  "*"                { return STAR; }
  "/"                { return SLASH; }
  "."                { return DOT; }
  "false"            { return KEYWORD_FALSE; }
  "true"             { return KEYWORD_TRUE; }
  "if"               { return KEYWORD_IF; }

  {IDENTIFIER}       { return IDENTIFIER; }
}

[^] { return BAD_CHARACTER; }
