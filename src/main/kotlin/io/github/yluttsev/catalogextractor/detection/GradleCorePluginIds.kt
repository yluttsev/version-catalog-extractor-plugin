package io.github.yluttsev.catalogextractor.detection

object GradleCorePluginIds {

    /**
     * Gradle core plugins are built in and should stay as plain plugin ids
     * instead of being extracted to version catalog plugin aliases.
     */
    private val CORE_PLUGIN_IDS = setOf(
        "antlr",
        "application",
        "base",
        "build-dashboard",
        "checkstyle",
        "distribution",
        "ear",
        "eclipse",
        "groovy",
        "idea",
        "jacoco",
        "java",
        "java-library",
        "java-platform",
        "maven-publish",
        "pmd",
        "scala",
        "signing",
        "war"
    )

    fun isCore(pluginId: String): Boolean = pluginId in CORE_PLUGIN_IDS
}
