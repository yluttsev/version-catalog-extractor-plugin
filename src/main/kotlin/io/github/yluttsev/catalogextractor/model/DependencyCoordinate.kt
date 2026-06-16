package io.github.yluttsev.catalogextractor.model

data class DependencyCoordinate(
    val groupId: String,
    val artifactId: String,
    val version: String
) {
    val module: String get() = "$groupId:$artifactId"
}
