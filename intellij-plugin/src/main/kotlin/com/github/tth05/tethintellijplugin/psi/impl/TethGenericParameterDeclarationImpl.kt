package com.github.tth05.tethintellijplugin.psi.impl

import com.github.tth05.tethintellijplugin.psi.api.TethGenericParameterDeclaration
import com.intellij.lang.ASTNode

class TethGenericParameterDeclarationImpl(node: ASTNode) : TethNamedElement(node), TethGenericParameterDeclaration {
}