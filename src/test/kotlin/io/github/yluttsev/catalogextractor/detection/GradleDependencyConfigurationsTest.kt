package io.github.yluttsev.catalogextractor.detection

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GradleDependencyConfigurationsTest {

    @Test
    fun `isKnown returns true for common JVM configurations`() {
        assertTrue(GradleDependencyConfigurations.isKnown("implementation"))
        assertTrue(GradleDependencyConfigurations.isKnown("api"))
        assertTrue(GradleDependencyConfigurations.isKnown("testImplementation"))
    }

    @Test
    fun `isKnown returns true for Android and tooling configurations`() {
        assertTrue(GradleDependencyConfigurations.isKnown("androidTestImplementation"))
        assertTrue(GradleDependencyConfigurations.isKnown("testFixturesImplementation"))
        assertTrue(GradleDependencyConfigurations.isKnown("lintChecks"))
        assertTrue(GradleDependencyConfigurations.isKnown("detektPlugins"))
    }

    @Test
    fun `isKnown returns false for unknown configuration`() {
        assertFalse(GradleDependencyConfigurations.isKnown("customImplementation"))
    }
}
