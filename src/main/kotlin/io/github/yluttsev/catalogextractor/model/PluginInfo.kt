package io.github.yluttsev.catalogextractor.model

data class PluginInfo(
    val coordinate: PluginCoordinate,
    val format: GradleFormat
) {
    val pluginId: String get() = coordinate.pluginId
    val version: String get() = coordinate.version
}
