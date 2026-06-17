package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import io.github.yluttsev.catalogextractor.detection.PluginDetector
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

class KotlinPluginDslInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!holder.file.name.endsWith(".gradle.kts")) return PsiElementVisitor.EMPTY_VISITOR

        return object : KtVisitorVoid() {
            override fun visitBinaryExpression(expression: KtBinaryExpression) {
                super.visitBinaryExpression(expression)
                val info = PluginDetector.detectInKotlinDsl(expression) ?: return

                PluginProblemReporter.register(holder, expression, info)
            }
        }
    }
}
