package io.github.yluttsev.catalogextractor.catalog

import io.github.yluttsev.catalogextractor.model.PluginInfo

object PluginAliasGenerator {

    fun generate(info: PluginInfo, existingAliases: Set<String>): String {
        val base = baseAlias(info.pluginId)
        if (base !in existingAliases) return base

        var suffixNumber = 2
        while (true) {
            val candidate = "$base-$suffixNumber"
            if (candidate !in existingAliases) return candidate
            suffixNumber++
        }
    }

    fun toAccessor(alias: String): String = "libs.plugins.${alias.replace('-', '.')}"

    private fun baseAlias(pluginId: String): String {
        val parts = pluginId
            .split('.')
            .dropWhile { it in LEADING_NAMESPACE_PARTS }
            .map { if (it == "springframework") "spring" else it }

        return sanitizeAliasPart(parts.joinToString("-"))
    }

    private fun sanitizeAliasPart(value: String): String =
        value
            .lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "-")
            .trim('-')

    private val LEADING_NAMESPACE_PARTS = setOf("com", "org", "io")
}
