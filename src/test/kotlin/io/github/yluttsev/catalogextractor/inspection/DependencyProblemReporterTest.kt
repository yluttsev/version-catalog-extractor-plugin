package io.github.yluttsev.catalogextractor.inspection

import com.intellij.openapi.util.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DependencyProblemReporterTest {

    @Test
    fun `findVersionRange returns range for double quoted literal`() {
        assertEquals(
            TextRange(33, 38),
            DependencyProblemReporter.findVersionRange("\"com.squareup.retrofit2:retrofit:2.9.0\"", "2.9.0")
        )
    }

    @Test
    fun `findVersionRange returns range for single quoted literal`() {
        assertEquals(
            TextRange(33, 38),
            DependencyProblemReporter.findVersionRange("'com.squareup.retrofit2:retrofit:2.9.0'", "2.9.0")
        )
    }

    @Test
    fun `findVersionRange returns last version occurrence`() {
        assertEquals(
            TextRange(16, 19),
            DependencyProblemReporter.findVersionRange("\"group-1.0:name:1.0\"", "1.0")
        )
    }

    @Test
    fun `findVersionRange returns null when version is absent`() {
        assertNull(DependencyProblemReporter.findVersionRange("\"group:name:1.0\"", "2.0"))
    }
}
