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
    fun `parseStringNotation rejects missing version`() {
        assertNull(GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit"))
    }

    @Test
    fun `parseStringNotation rejects interpolation syntax`() {
        assertNull(GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit:${'$'}version"))
    }

    @Test
    fun `parseStringNotation rejects whitespace`() {
        assertNull(GradleDependencyNotationParser.parseStringNotation("com.squareup.retrofit2:retrofit:2.9.0 "))
    }
}
