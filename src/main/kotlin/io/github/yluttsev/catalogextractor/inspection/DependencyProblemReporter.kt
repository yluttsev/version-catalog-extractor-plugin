package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import io.github.yluttsev.catalogextractor.CatalogExtractorBundle
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.quickfix.ExtractToVersionCatalogFix

object DependencyProblemReporter {

    fun register(holder: ProblemsHolder, literal: PsiElement, info: DependencyInfo) {
        val notationRange = findNotationRange(literal.text, info.notation) ?: return

        holder.registerProblem(
            literal,
            CatalogExtractorBundle.message("inspection.dependency.problem.description"),
            ProblemHighlightType.WARNING,
            notationRange,
            ExtractToVersionCatalogFix(info)
        )
    }

    internal fun findNotationRange(literalText: String, notation: String): TextRange? {
        val notationStart = literalText.indexOf(notation)
        if (notationStart == -1) return null

        return TextRange(notationStart, notationStart + notation.length)
    }
}
