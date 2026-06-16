package io.github.yluttsev.catalogextractor.catalog

import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.model.DependencyCoordinate
import io.github.yluttsev.catalogextractor.model.GradleFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class AliasGeneratorTest {

    private fun buildDependencyInfo(groupId: String, artifactId: String) = DependencyInfo(
        configuration = "implementation",
        coordinate = DependencyCoordinate(
            groupId = groupId,
            artifactId = artifactId,
            version = "1.0.0"
        ),
        format = GradleFormat.KOTLIN_DSL
    )

    @Test
    fun `simple artifactId becomes alias`() {
        val info = buildDependencyInfo("com.squareup.retrofit2", "retrofit")
        assertEquals("retrofit", AliasGenerator.generate(info, emptySet()))
    }

    @Test
    fun `dots in artifactId replaced with dashes`() {
        val info = buildDependencyInfo("com.example", "my.library.core")
        assertEquals("my-library-core", AliasGenerator.generate(info, emptySet()))
    }

    @Test
    fun `non alphanumeric artifactId characters are replaced with dashes`() {
        val info = buildDependencyInfo("com.example", "my_library.core")
        assertEquals("my-library-core", AliasGenerator.generate(info, emptySet()))
    }

    @Test
    fun `conflict resolved with groupId suffix`() {
        val info = buildDependencyInfo("com.squareup.retrofit2", "retrofit")
        assertEquals("retrofit2-retrofit", AliasGenerator.generate(info, setOf("retrofit")))
    }

    @Test
    fun `groupId suffix is sanitized when resolving conflict`() {
        val info = buildDependencyInfo("com.Squareup.Retrofit_2", "retrofit")
        assertEquals("retrofit-2-retrofit", AliasGenerator.generate(info, setOf("retrofit")))
    }

    @Test
    fun `double conflict resolved with numeric suffix`() {
        val info = buildDependencyInfo("com.squareup.retrofit2", "retrofit")
        assertEquals("retrofit2-retrofit-2", AliasGenerator.generate(info, setOf("retrofit", "retrofit2-retrofit")))
    }

    @Test
    fun `alias is always lowercase`() {
        val info = buildDependencyInfo("com.Example", "MyLib")
        assertEquals("mylib", AliasGenerator.generate(info, emptySet()))
    }

    @Test
    fun `toAccessor converts dashes to dots`() {
        assertEquals("libs.retrofit", AliasGenerator.toAccessor("retrofit"))
        assertEquals("libs.kotlin.stdlib", AliasGenerator.toAccessor("kotlin-stdlib"))
        assertEquals("libs.squareup.okhttp3.okhttp", AliasGenerator.toAccessor("squareup-okhttp3-okhttp"))
    }
}
