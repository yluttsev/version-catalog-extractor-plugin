package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.quickfix.ExtractToVersionCatalogFix

object DependencyProblemReporter {

    fun register(holder: ProblemsHolder, literal: PsiElement, info: DependencyInfo) {
        val versionRange = findVersionRange(literal.text, info.version) ?: return

        holder.registerProblem(
            literal,
            "Hardcoded dependency version '${info.version}'",
            ProblemHighlightType.WARNING,
            versionRange,
            ExtractToVersionCatalogFix(info)
        )
    }

    internal fun findVersionRange(literalText: String, version: String): TextRange? {
        val versionStart = literalText.lastIndexOf(version)
        if (versionStart == -1) return null

        return TextRange(versionStart, versionStart + version.length)
    }
}
