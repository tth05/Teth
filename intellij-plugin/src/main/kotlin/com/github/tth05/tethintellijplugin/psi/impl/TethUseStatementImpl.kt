package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.TethElementTypes
import com.github.tth05.tethintellijplugin.psi.api.TethIdentifierLiteralExpression
import com.github.tth05.tethintellijplugin.psi.api.TethUseStatement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class TethUseStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethUseStatement {
    override val moduleName: TethIdentifierLiteralExpression
        get() = findNotNullChildByClass(TethIdentifierLiteralExpression::class.java)
    override val imports: List<TethIdentifierLiteralExpression>
        get() = findChildrenByType<TethIdentifierLiteralExpression>(TethElementTypes.IDENTIFIER_LITERAL).apply {
            removeFirst()
        }
}