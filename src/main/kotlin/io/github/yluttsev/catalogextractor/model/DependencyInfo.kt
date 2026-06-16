package io.github.yluttsev.catalogextractor.model

data class DependencyInfo(
    val configuration: String,
    val coordinate: DependencyCoordinate,
    val format: GradleFormat
) {
    val groupId: String get() = coordinate.groupId
    val artifactId: String get() = coordinate.artifactId
    val version: String get() = coordinate.version
    val module: String get() = coordinate.module
}
