package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import io.github.yluttsev.catalogextractor.detection.DependencyDetector
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

class KotlinDslInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!holder.file.name.endsWith(".gradle.kts")) return PsiElementVisitor.EMPTY_VISITOR

        return object : KtVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)
                val info = DependencyDetector.detectInKotlinDsl(expression) ?: return
                val template = expression.valueArgumentList
                    ?.arguments?.firstOrNull()
                    ?.getArgumentExpression() as? KtStringTemplateExpression ?: return

                DependencyProblemReporter.register(holder, template, info)
            }
        }
    }
}
