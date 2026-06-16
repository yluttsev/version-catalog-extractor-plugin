package io.github.yluttsev.catalogextractor.quickfix

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.yluttsev.catalogextractor.catalog.AliasGenerator
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.model.GradleFormat
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall

class GradleDependencyUsageReplacer {

    fun prepare(element: PsiElement, info: DependencyInfo): ReplacementTarget? {
        val callExpression = findCallExpression(element, info)
        if (callExpression == null) {
            LOG.warn("Unable to find dependency call expression for ${info.module}")
            return null
        }

        val containingFile = element.containingFile
        return ReplacementTarget(
            file = containingFile,
            fileName = containingFile.name,
            range = callExpression.textRange
        )
    }

    fun replace(element: PsiElement, info: DependencyInfo, alias: String, project: Project): Boolean {
        val target = prepare(element, info) ?: return false
        return replace(target, info, alias, project)
    }

    fun replace(target: ReplacementTarget, info: DependencyInfo, alias: String, project: Project): Boolean {
        val document = PsiDocumentManager.getInstance(project).getDocument(target.file)
        if (document == null) {
            LOG.warn("Unable to find document for ${target.fileName}")
            return false
        }

        val accessor = AliasGenerator.toAccessor(alias)
        val replacement = when (info.format) {
            GradleFormat.KOTLIN_DSL -> "${info.configuration}($accessor)"
            GradleFormat.GROOVY_DSL -> "${info.configuration} $accessor"
        }

        document.replaceString(
            target.range.startOffset,
            target.range.endOffset,
            replacement
        )
        PsiDocumentManager.getInstance(project).commitDocument(document)
        LOG.debug("Replaced ${info.module} with '$accessor' in ${target.fileName}")
        return true
    }

    private fun findCallExpression(element: PsiElement, info: DependencyInfo): PsiElement? = when (info.format) {
        GradleFormat.KOTLIN_DSL -> PsiTreeUtil.getParentOfType(element, KtCallExpression::class.java, false)
        GradleFormat.GROOVY_DSL -> PsiTreeUtil.getParentOfType(element, GrCall::class.java, false)
    }

    companion object {
        private val LOG = Logger.getInstance(GradleDependencyUsageReplacer::class.java)
    }

    data class ReplacementTarget(
        val file: PsiFile,
        val fileName: String,
        val range: TextRange
    )
}
