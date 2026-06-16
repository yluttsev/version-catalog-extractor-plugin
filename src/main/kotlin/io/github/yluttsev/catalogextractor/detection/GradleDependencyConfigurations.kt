package io.github.yluttsev.catalogextractor.detection

object GradleDependencyConfigurations {

    private val KNOWN = setOf(
        "implementation",
        "api",
        "compileOnly",
        "runtimeOnly",
        "compileOnlyApi",
        "testImplementation",
        "testCompileOnly",
        "testRuntimeOnly",
        "testFixturesImplementation",
        "testFixturesApi",
        "testFixturesCompileOnly",
        "testFixturesRuntimeOnly",
        "androidTestImplementation",
        "androidTestCompileOnly",
        "androidTestRuntimeOnly",
        "kapt",
        "kaptTest",
        "kaptAndroidTest",
        "ksp",
        "kspTest",
        "kspAndroidTest",
        "annotationProcessor",
        "debugImplementation",
        "releaseImplementation",
        "lintChecks",
        "lintPublish",
        "detektPlugins"
    )

    fun isKnown(configuration: String): Boolean = configuration in KNOWN
}
