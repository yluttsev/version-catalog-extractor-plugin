package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import io.github.yluttsev.catalogextractor.detection.DependencyDetector
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementVisitor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression

class GroovyDslInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!holder.file.name.endsWith(".gradle")) return PsiElementVisitor.EMPTY_VISITOR

        return GroovyPsiElementVisitor(object : GroovyElementVisitor() {
            override fun visitMethodCallExpression(methodCallExpression: GrMethodCallExpression) {
                report(methodCallExpression, holder)
            }

            override fun visitApplicationStatement(applicationStatement: GrApplicationStatement) {
                report(applicationStatement, holder)
            }
        })
    }

    private fun report(call: GrCall, holder: ProblemsHolder) {
        val info = DependencyDetector.detectInGroovyDsl(call) ?: return
        val literal = call.expressionArguments.firstOrNull() as? GrLiteral ?: return

        DependencyProblemReporter.register(holder, literal, info)
    }
}
