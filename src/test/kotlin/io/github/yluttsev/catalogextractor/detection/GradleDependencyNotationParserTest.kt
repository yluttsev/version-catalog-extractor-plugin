package io.github.yluttsev.catalogextractor.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradleDependencyNotationParserTest {

    @Test
    fun `parseStringNotation returns dependency coordinates`() {
        val coordinate = GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit:2.9.0")

        assertEquals("com.squareup.retrofit2", coordinate?.groupId)
        assertEquals("retrofit", coordinate?.artifactId)
        assertEquals("2.9.0", coordinate?.version)
        assertEquals("com.squareup.retrofit2:retrofit", coordinate?.module)
    }

    @Test
    fun `parseStringNotation returns dependency coordinates without version`() {
        val coordinate = GradleDependencyNotationParser.parseStringNotation("org.springframework.boot:spring-boot-starter-web")

        assertEquals("org.springframework.boot", coordinate?.groupId)
        assertEquals("spring-boot-starter-web", coordinate?.artifactId)
        assertNull(coordinate?.version)
        assertEquals("org.springframework.boot:spring-boot-starter-web", coordinate?.module)
        assertEquals("org.springframework.boot:spring-boot-starter-web", coordinate?.notation)
    }

    @Test
    fun `parseStringNotation rejects interpolation syntax`() {
        assertNull(GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit:${'$'}version"))
    }

    @Test
    fun `parseStringNotation rejects whitespace`() {
        assertNull(GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit:2.9.0 "))
    }

    @Test
    fun `parseStringNotation rejects classifier notation`() {
        assertNull(GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit:2.9.0:javadoc"))
    }
}
