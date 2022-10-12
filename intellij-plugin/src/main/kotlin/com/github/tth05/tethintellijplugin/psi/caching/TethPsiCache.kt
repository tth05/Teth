package com.github.tth05.tethintellijplugin.psi.caching

import com.github.tth05.teth.analyzer.Analyzer
import com.github.tth05.teth.analyzer.AnalyzerResult
import com.github.tth05.teth.lang.parser.ParserResult
import com.github.tth05.teth.lang.parser.SourceFileUnit
import com.github.tth05.tethintellijplugin.psi.TethPsiFile
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.atomic.AtomicReference

class TethPsiCache(project: Project) : Disposable {
    private val globalCache = AtomicReference(makeWeakMap())

    init {
        PsiManager.getInstance(project).addPsiTreeChangeListener(object : StubPsiTreeChangeListener() {
            override fun handleChange(file: PsiFile?) {
                if (file !is TethPsiFile)
                    return

                globalCache.set(makeWeakMap())
            }
        }, this)
    }

    override fun dispose() {}

    fun putValue(key: PsiElement, value: Any) {
        globalCache.get()[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : Any> getValue(key: PsiElement): V? {
        return globalCache.get()[key] as V?
    }

    @Suppress("UNCHECKED_CAST")
    fun <K : PsiElement, V : Any> resolveWithCaching(key: K, resolver: (K) -> V): V {
        ProgressManager.checkCanceled()
        return globalCache.get().getOrPut(key) { resolver(key) } as V
    }
}

fun PsiElement.tethCache() = project.service<TethPsiCache>()

private abstract class StubPsiTreeChangeListener : PsiTreeChangeListener {
    abstract fun handleChange(file: PsiFile?)

    override fun beforeChildAddition(event: PsiTreeChangeEvent) {
    }

    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {
    }

    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {
    }

    override fun beforeChildMovement(event: PsiTreeChangeEvent) {
    }

    override fun beforeChildrenChange(event: PsiTreeChangeEvent) {
    }

    override fun beforePropertyChange(event: PsiTreeChangeEvent) {
    }

    override fun childAdded(event: PsiTreeChangeEvent) {
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        handleChange(event.file)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
    }

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        handleChange(event.file)
    }

}

private fun makeWeakMap() = ContainerUtil.createConcurrentWeakKeySoftValueMap<PsiElement, Any>()