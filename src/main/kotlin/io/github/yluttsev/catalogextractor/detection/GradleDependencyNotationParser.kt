package io.github.yluttsev.catalogextractor.detection

import io.github.yluttsev.catalogextractor.model.DependencyCoordinate

object GradleDependencyNotationParser {

    private val STRING_NOTATION_REGEX = Regex($$"""^([^:\s]+):([^:\s]+):([^:${}\s]+)$""")

    fun parseStringNotation(text: String): DependencyCoordinate? {
        val match = STRING_NOTATION_REGEX.matchEntire(text) ?: return null
        val (groupId, artifactId, version) = match.destructured
        return DependencyCoordinate(
            groupId = groupId,
            artifactId = artifactId,
            version = version
        )
    }
}
