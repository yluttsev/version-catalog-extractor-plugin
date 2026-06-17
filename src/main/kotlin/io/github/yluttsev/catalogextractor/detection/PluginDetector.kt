package io.github.yluttsev.catalogextractor.detection

import io.github.yluttsev.catalogextractor.model.GradleFormat
import io.github.yluttsev.catalogextractor.model.PluginCoordinate
import io.github.yluttsev.catalogextractor.model.PluginInfo
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

object PluginDetector {

    fun detectInKotlinDsl(expression: KtBinaryExpression): PluginInfo? {
        if (!expression.isInsidePluginsBlock()) return null
        if (expression.operationReference.text != "version") return null

        val idCall = expression.left as? KtCallExpression ?: return null
        if (idCall.calleeExpression?.text != "id") return null

        val pluginId = idCall.singleStringLiteralArgument() ?: return null
        if (GradleCorePluginIds.isCore(pluginId)) return null

        val version = (expression.right as? KtStringTemplateExpression)?.textWithoutInterpolation() ?: return null

        return PluginInfo(
            coordinate = PluginCoordinate(pluginId = pluginId, version = version),
            format = GradleFormat.KOTLIN_DSL
        )
    }

    private fun KtCallExpression.singleStringLiteralArgument(): String? {
        val args = valueArgumentList?.arguments ?: return null
        if (args.size != 1) return null

        val template = args[0].getArgumentExpression() as? KtStringTemplateExpression ?: return null
        return template.textWithoutInterpolation()
    }

    private fun KtStringTemplateExpression.textWithoutInterpolation(): String? {
        if (hasInterpolation()) return null
        return entries.joinToString("") { it.text }
    }

    private fun KtBinaryExpression.isInsidePluginsBlock(): Boolean {
        var current = parent
        while (current != null) {
            if (current is KtCallExpression && current.calleeExpression?.text == "plugins") return true
            current = current.parent
        }
        return false
    }
}
