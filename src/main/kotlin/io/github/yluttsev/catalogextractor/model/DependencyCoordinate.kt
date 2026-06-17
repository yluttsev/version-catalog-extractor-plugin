package io.github.yluttsev.catalogextractor.model

data class DependencyCoordinate(
    val groupId: String,
    val artifactId: String,
    val version: String?
) {
    val module: String get() = "$groupId:$artifactId"

    /**
     * Gradle string notation without quotes, using `group:name` or
     * `group:name:version` depending on whether [version] is present.
     */
    val notation: String get() = version?.let { "$module:$it" } ?: module
}
