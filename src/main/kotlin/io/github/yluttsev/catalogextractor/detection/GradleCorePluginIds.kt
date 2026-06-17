package io.github.yluttsev.catalogextractor.detection

object GradleCorePluginIds {

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
