package io.github.yluttsev.catalogextractor.detection

import io.github.yluttsev.catalogextractor.model.DependencyCoordinate

object GradleDependencyNotationParser {

    /**
     * Parses literal Gradle dependency notation in supported forms:
     * `group:name` and `group:name:version`.
     *
     * Returns `null` for interpolated, whitespace-containing, or extended notations.
     */
    fun parseStringNotation(text: String): DependencyCoordinate? {
        if (text.isBlank() || text.any { it.isWhitespace() } || text.contains('$')) return null

        val parts = text.split(':')
        if (parts.size !in 2..3 || parts.any { it.isBlank() }) return null

        return DependencyCoordinate(
            groupId = parts[0],
            artifactId = parts[1],
            version = parts.getOrNull(2)
        )
    }
}
