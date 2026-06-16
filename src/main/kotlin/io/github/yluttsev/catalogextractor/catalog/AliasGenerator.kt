package io.github.yluttsev.catalogextractor.catalog

import io.github.yluttsev.catalogextractor.model.DependencyInfo

object AliasGenerator {

    fun generate(info: DependencyInfo, existingAliases: Set<String>): String {
        val base = sanitizeAliasPart(info.artifactId)
        if (base !in existingAliases) return base

        val groupSuffix = sanitizeAliasPart(info.groupId.substringAfterLast('.'))
        val withSuffix = "$groupSuffix-$base"
        if (withSuffix !in existingAliases) return withSuffix

        var suffixNumber = 2
        while (true) {
            val candidate = "$withSuffix-$suffixNumber"
            if (candidate !in existingAliases) return candidate
            suffixNumber++
        }
    }

    fun toAccessor(alias: String): String = "libs.${alias.replace('-', '.')}"

    private fun sanitizeAliasPart(value: String): String =
        value
            .lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "-")
            .trim('-')
}
