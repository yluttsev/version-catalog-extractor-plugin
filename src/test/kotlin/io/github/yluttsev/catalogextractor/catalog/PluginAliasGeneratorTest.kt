package io.github.yluttsev.catalogextractor.catalog

import io.github.yluttsev.catalogextractor.model.GradleFormat
import io.github.yluttsev.catalogextractor.model.PluginCoordinate
import io.github.yluttsev.catalogextractor.model.PluginInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class PluginAliasGeneratorTest {

    @Test
    fun `generate creates alias from plugin id`() {
        assertEquals("spring-boot", PluginAliasGenerator.generate(pluginInfo("org.springframework.boot"), emptySet()))
    }

    @Test
    fun `generate appends numeric suffix for duplicate aliases`() {
        assertEquals("spring-boot-2", PluginAliasGenerator.generate(pluginInfo("org.springframework.boot"), setOf("spring-boot")))
    }

    @Test
    fun `toAccessor returns plugin catalog accessor`() {
        assertEquals("libs.plugins.spring.boot", PluginAliasGenerator.toAccessor("spring-boot"))
    }

    private fun pluginInfo(pluginId: String): PluginInfo =
        PluginInfo(
            coordinate = PluginCoordinate(pluginId = pluginId, version = "3.5.0"),
            format = GradleFormat.KOTLIN_DSL
        )
}
