package io.github.yluttsev.catalogextractor.quickfix

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import io.github.yluttsev.catalogextractor.catalog.PluginAliasGenerator
import io.github.yluttsev.catalogextractor.model.PluginInfo
import org.jetbrains.kotlin.psi.KtBinaryExpression

class GradlePluginUsageReplacer {

    fun prepare(element: PsiElement, info: PluginInfo): ReplacementTarget? {
        val binaryExpression = PsiTreeUtil.getParentOfType(element, KtBinaryExpression::class.java, false)
        if (binaryExpression == null) {
            LOG.warn("Unable to find plugin declaration for ${info.pluginId}")
            return null
        }

        val containingFile = element.containingFile
        return ReplacementTarget(
            file = containingFile,
            fileName = containingFile.name,
            range = binaryExpression.textRange
        )
    }

    fun replace(target: ReplacementTarget, info: PluginInfo, alias: String, project: Project): Boolean {
        val document = PsiDocumentManager.getInstance(project).getDocument(target.file)
        if (document == null) {
            LOG.warn("Unable to find document for ${target.fileName}")
            return false
        }

        val accessor = PluginAliasGenerator.toAccessor(alias)
        document.replaceString(
            target.range.startOffset,
            target.range.endOffset,
            "alias($accessor)"
        )
        PsiDocumentManager.getInstance(project).commitDocument(document)
        LOG.debug("Replaced ${info.pluginId} with '$accessor' in ${target.fileName}")
        return true
    }

    companion object {
        private val LOG = Logger.getInstance(GradlePluginUsageReplacer::class.java)
    }

    data class ReplacementTarget(
        val file: PsiFile,
        val fileName: String,
        val range: TextRange
    )
}
