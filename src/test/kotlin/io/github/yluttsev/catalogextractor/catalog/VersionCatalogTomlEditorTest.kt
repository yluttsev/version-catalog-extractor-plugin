package io.github.yluttsev.catalogextractor.catalog

import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.model.DependencyCoordinate
import io.github.yluttsev.catalogextractor.model.GradleFormat
import io.github.yluttsev.catalogextractor.model.PluginCoordinate
import io.github.yluttsev.catalogextractor.model.PluginInfo
import org.junit.Assert.*
import org.junit.Test

class VersionCatalogTomlEditorTest {

    private val sampleToml = """
        [libraries]
        junit = { module = "junit:junit", version.ref = "junit" }

        [plugins]

        [versions]
        junit = "4.13.2"
    """.trimIndent()

    private fun buildDependencyInfo(groupId: String, artifactId: String, version: String? = "2.9.0") = DependencyInfo(
        configuration = "implementation",
        coordinate = DependencyCoordinate(
            groupId = groupId,
            artifactId = artifactId,
            version = version
        ),
        format = GradleFormat.KOTLIN_DSL
    )

    private fun buildPluginInfo(pluginId: String, version: String = "3.5.0") = PluginInfo(
        coordinate = PluginCoordinate(
            pluginId = pluginId,
            version = version
        ),
        format = GradleFormat.KOTLIN_DSL
    )

    @Test
    fun `findExistingAlias returns alias for known module`() {
        assertEquals("junit", VersionCatalogTomlEditor.findExistingAlias(sampleToml, "junit:junit"))
    }

    @Test
    fun `findExistingAlias returns alias for group and name notation`() {
        val toml = """
            [libraries]
            retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }

            [versions]
            retrofit = "2.9.0"
        """.trimIndent()

        assertEquals("retrofit", VersionCatalogTomlEditor.findExistingAlias(toml, "com.squareup.retrofit2:retrofit"))
    }

    @Test
    fun `findExistingAlias returns alias for group and name notation in any order`() {
        val toml = """
            [libraries]
            retrofit = { name = "retrofit", version.ref = "retrofit", group = "com.squareup.retrofit2" }

            [versions]
            retrofit = "2.9.0"
        """.trimIndent()

        assertEquals("retrofit", VersionCatalogTomlEditor.findExistingAlias(toml, "com.squareup.retrofit2:retrofit"))
    }

    @Test
    fun `findExistingAlias returns null for unknown module`() {
        assertNull(VersionCatalogTomlEditor.findExistingAlias(sampleToml, "com.squareup.retrofit2:retrofit"))
    }

    @Test
    fun `getAllAliases returns all library aliases`() {
        assertEquals(setOf("junit"), VersionCatalogTomlEditor.getAllAliases(sampleToml))
    }

    @Test
    fun `findExistingPluginAlias returns alias for known plugin id`() {
        val toml = """
            [plugins]
            spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }

            [versions]
            spring-boot = "3.5.0"
        """.trimIndent()

        assertEquals("spring-boot", VersionCatalogTomlEditor.findExistingPluginAlias(toml, "org.springframework.boot"))
    }

    @Test
    fun `getAllPluginAliases returns all plugin aliases`() {
        val toml = """
            [plugins]
            spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
        """.trimIndent()

        assertEquals(setOf("spring-boot"), VersionCatalogTomlEditor.getAllPluginAliases(toml))
    }

    @Test
    fun `addEntry inserts into libraries and versions`() {
        val result = VersionCatalogTomlEditor.addEntry(sampleToml, "retrofit", buildDependencyInfo("com.squareup.retrofit2", "retrofit"))
        assertTrue(result.contains("""retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }"""))
        assertTrue(result.contains("""retrofit = "2.9.0""""))
    }

    @Test
    fun `addEntry inserts versionless dependency into libraries only`() {
        val result = VersionCatalogTomlEditor.addEntry(
            sampleToml,
            "spring-boot-starter-web",
            buildDependencyInfo("org.springframework.boot", "spring-boot-starter-web", version = null)
        )

        assertTrue(result.contains("""spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }"""))
        assertFalse(result.contains("""spring-boot-starter-web = """"))
        assertFalse(result.contains("""version.ref = "spring-boot-starter-web""""))
    }

    @Test
    fun `addEntry does not create versions section for versionless dependency`() {
        val toml = """
            [libraries]
            junit = { module = "junit:junit" }
        """.trimIndent()

        val result = VersionCatalogTomlEditor.addEntry(
            toml,
            "spring-boot-starter-web",
            buildDependencyInfo("org.springframework.boot", "spring-boot-starter-web", version = null)
        )

        assertFalse(result.contains("[versions]"))
    }

    @Test
    fun `addEntry creates missing libraries section`() {
        val toml = """
            [versions]
            junit = "4.13.2"
        """.trimIndent()

        val result = VersionCatalogTomlEditor.addEntry(toml, "retrofit", buildDependencyInfo("com.squareup.retrofit2", "retrofit"))

        assertTrue(result.contains("[libraries]"))
        assertTrue(result.contains("""retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }"""))
    }

    @Test
    fun `addEntry creates missing versions section`() {
        val toml = """
            [libraries]
            junit = { module = "junit:junit", version.ref = "junit" }
        """.trimIndent()

        val result = VersionCatalogTomlEditor.addEntry(toml, "retrofit", buildDependencyInfo("com.squareup.retrofit2", "retrofit"))

        assertTrue(result.contains("[versions]"))
        assertTrue(result.contains("""retrofit = "2.9.0""""))
    }

    @Test
    fun `addEntry preserves section order libraries then plugins then versions`() {
        val result = VersionCatalogTomlEditor.addEntry(sampleToml, "retrofit", buildDependencyInfo("com.squareup.retrofit2", "retrofit"))
        assertTrue(result.indexOf("[libraries]") < result.indexOf("[plugins]"))
        assertTrue(result.indexOf("[plugins]") < result.indexOf("[versions]"))
    }

    @Test
    fun `addEntry puts library entry before plugins section`() {
        val result = VersionCatalogTomlEditor.addEntry(sampleToml, "retrofit", buildDependencyInfo("com.squareup.retrofit2", "retrofit"))
        assertTrue(result.indexOf("""retrofit = { module""") < result.indexOf("[plugins]"))
    }

    @Test
    fun `addEntry puts version entry after versions section header`() {
        val result = VersionCatalogTomlEditor.addEntry(sampleToml, "retrofit", buildDependencyInfo("com.squareup.retrofit2", "retrofit"))
        assertTrue(result.indexOf("""retrofit = "2.9.0"""") > result.indexOf("[versions]"))
    }

    @Test
    fun `addPluginEntry inserts into plugins and versions`() {
        val result = VersionCatalogTomlEditor.addPluginEntry(sampleToml, "spring-boot", buildPluginInfo("org.springframework.boot"))

        assertTrue(result.contains("""spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }"""))
        assertTrue(result.contains("""spring-boot = "3.5.0""""))
    }

    @Test
    fun `createEmptyCatalogContent has correct section order`() {
        val toml = VersionCatalogTomlEditor.createEmptyCatalogContent()
        assertTrue(toml.indexOf("[libraries]") < toml.indexOf("[plugins]"))
        assertTrue(toml.indexOf("[plugins]") < toml.indexOf("[versions]"))
    }
}
