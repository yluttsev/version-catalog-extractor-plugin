package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import io.github.yluttsev.catalogextractor.CatalogExtractorBundle
import io.github.yluttsev.catalogextractor.model.PluginInfo
import io.github.yluttsev.catalogextractor.quickfix.ExtractPluginToVersionCatalogFix

object PluginProblemReporter {

    fun register(holder: ProblemsHolder, element: PsiElement, info: PluginInfo) {
        holder.registerProblem(
            element,
            CatalogExtractorBundle.message("inspection.plugin.problem.description"),
            ProblemHighlightType.WARNING,
            ExtractPluginToVersionCatalogFix(info)
        )
    }
}
