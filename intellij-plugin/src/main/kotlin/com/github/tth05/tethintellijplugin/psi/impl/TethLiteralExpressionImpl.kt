package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.teth.lang.parser.ASTUtil
import com.github.tth05.teth.lang.parser.ASTVisitor
import com.github.tth05.teth.lang.parser.ast.IDeclarationReference
import com.github.tth05.teth.lang.parser.ast.IdentifierExpression
import com.github.tth05.teth.lang.parser.ast.Statement
import com.github.tth05.tethintellijplugin.psi.api.*
import com.github.tth05.tethintellijplugin.psi.caching.tethCache
import com.github.tth05.tethintellijplugin.psi.reference.TethReference
import com.github.tth05.tethintellijplugin.psi.reference.resolvedRefTo
import com.github.tth05.tethintellijplugin.syntax.analyzeAndParseFile
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.findParentOfType
import com.intellij.refactoring.suggested.startOffset

class TethLongLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethLongLiteralExpression

class TethDoubleLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethDoubleLiteralExpression

class TethStringLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethStringLiteralExpression

class TethBooleanLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethBooleanLiteralExpression

class TethIdentifierLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethIdentifierLiteralExpression {
    override val identifier: String?
        get() = text.emptyToNull()

    override fun getReference(): PsiReference? {
        // Get analyzed file
        val file = containingFile ?: return null
        val (analyzer, _, result) = file.tethCache().resolveWithCaching(file) {
            analyzeAndParseFile(file)
        }

        return tethCache().resolveWithCaching(this) {
            // Convert psi element to teth ast node and get reference
            val target: Statement = analyzer.resolvedReference(
                ASTUtil.findStatementAtExact(
                    result.unit,
                    startOffset
                ) as? IDeclarationReference ?: return@resolveWithCaching TethReference.UNRESOLVED
            ) ?: return@resolveWithCaching TethReference.UNRESOLVED

            // Convert teth ast node to psi element
            val psiTarget: PsiElement =
                findDeclarationAt(target.span!!.offset) ?: return@resolveWithCaching TethReference.UNRESOLVED

            // Create reference
            return@resolveWithCaching resolvedRefTo(psiTarget)
        }.takeIf { it != TethReference.UNRESOLVED }
    }
}

class TethListLiteralExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethListLiteralExpression {
    override val initializers: List<TethExpression>
        get() = childrenOfType()
}

class TethTypeExpressionImpl(node: ASTNode) : ASTWrapperPsiElement(node), TethTypeExpression {
    override val typeName: TethIdentifierLiteralExpression
        get() = findNotNullChildByClass(TethIdentifierLiteralExpression::class.java)
    override val genericArguments: List<TethTypeExpression>
        get() = childrenOfType()
}

private fun String.emptyToNull() = ifEmpty { null }

private fun PsiElement.findDeclarationAt(offset: Int): PsiElement? {
    fun isMatch(current: PsiElement?, offset: Int) = current is PsiNameIdentifierOwner && current.startOffset == offset
    fun checkChildren(current: PsiElement, offset: Int): PsiElement? {
        if (current.startOffset > offset) return null

        for (child in current.children) {
            if (isMatch(child, offset)) return child

            checkChildren(child, offset)?.let { return it }
        }

        return null
    }

    return checkChildren(findParentOfType<TethUnit>()!!, offset)
}