package io.github.yluttsev.catalogextractor.detection

import io.github.yluttsev.catalogextractor.model.DependencyCoordinate
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.model.GradleFormat
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.plugins.groovy.lang.psi.api.GrFunctionalExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression

object DependencyDetector {

    fun detectInKotlinDsl(call: KtCallExpression): DependencyInfo? {
        val configuration = call.calleeExpression?.text ?: return null
        if (!GradleDependencyConfigurations.isKnown(configuration)) return null

        val args = call.valueArgumentList?.arguments ?: return null
        if (args.size != 1) return null

        val arg = args[0].getArgumentExpression() ?: return null

        if (arg is KtDotQualifiedExpression) return null

        val template = arg as? KtStringTemplateExpression ?: return null
        if (template.hasInterpolation()) return null

        val text = template.entries.joinToString("") { it.text }
        return GradleDependencyNotationParser.parseStringNotation(text)
            ?.toDependencyInfo(configuration, GradleFormat.KOTLIN_DSL)
    }

    fun detectInGroovyDsl(call: GrCall): DependencyInfo? {
        val configuration = invokedExpressionText(call) ?: return null
        if (!GradleDependencyConfigurations.isKnown(configuration)) return null

        val args = call.expressionArguments
        if (args.size != 1) return null

        val arg = args[0]

        if (arg.text.startsWith("libs.")) return null
        if (arg is GrFunctionalExpression) return null

        val literal = arg as? GrLiteral ?: return null
        val value = literal.value as? String ?: return null

        return GradleDependencyNotationParser.parseStringNotation(value)
            ?.toDependencyInfo(configuration, GradleFormat.GROOVY_DSL)
    }

    private fun invokedExpressionText(call: GrCall): String? = when (call) {
        is GrMethodCallExpression -> call.invokedExpression.text
        is GrApplicationStatement -> call.invokedExpression.text
        else -> null
    }

    private fun DependencyCoordinate.toDependencyInfo(configuration: String, format: GradleFormat): DependencyInfo =
        DependencyInfo(
            configuration = configuration,
            coordinate = this,
            format = format
        )
}
