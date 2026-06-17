package io.github.yluttsev.catalogextractor.inspection

import com.intellij.openapi.util.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DependencyProblemReporterTest {

    @Test
    fun `findNotationRange returns range for double quoted literal`() {
        assertEquals(
            TextRange(1, 38),
            DependencyProblemReporter.findNotationRange(
                "\"com.squareup.retrofit2:retrofit:2.9.0\"",
                "com.squareup.retrofit2:retrofit:2.9.0"
            )
        )
    }

    @Test
    fun `findNotationRange returns range for single quoted literal`() {
        assertEquals(
            TextRange(1, 38),
            DependencyProblemReporter.findNotationRange(
                "'com.squareup.retrofit2:retrofit:2.9.0'",
                "com.squareup.retrofit2:retrofit:2.9.0"
            )
        )
    }

    @Test
    fun `findNotationRange returns range for versionless dependency`() {
        assertEquals(
            TextRange(1, 49),
            DependencyProblemReporter.findNotationRange(
                "\"org.springframework.boot:spring-boot-starter-web\"",
                "org.springframework.boot:spring-boot-starter-web"
            )
        )
    }

    @Test
    fun `findNotationRange returns null when notation is absent`() {
        assertNull(DependencyProblemReporter.findNotationRange("\"group:name:1.0\"", "other:name"))
    }
}
