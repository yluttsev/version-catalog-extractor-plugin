package io.github.yluttsev.catalogextractor.detection

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.yluttsev.catalogextractor.quickfix.ExtractToVersionCatalogFix
import org.jetbrains.plugins.groovy.GroovyFileType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class GroovyDslDependencyDetectorTest : BasePlatformTestCase() {

    fun `test detects hardcoded dependency version`() {
        configureGroovyBuildFile()

        val info = detectDependency()

        assertNotNull(info)
        assertEquals("implementation", info?.configuration)
        assertEquals("com.squareup.retrofit2", info?.groupId)
        assertEquals("retrofit", info?.artifactId)
        assertEquals("2.9.0", info?.version)
    }

    fun `test quick fix extracts dependency to new version catalog`() {
        configureGroovyBuildFile()

        val info = requireNotNull(detectDependency())
        val literal = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, GrLiteral::class.java))
        ExtractToVersionCatalogFix(info).applyFix(project, literal)

        myFixture.checkResult(
            """
                dependencies {
                    implementation libs.retrofit
                }
            """.trimIndent()
        )

        val catalog = requireNotNull(catalogFile())
        val catalogContent = VfsUtilCore.loadText(catalog)
        assertTrue(catalogContent.contains("""retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }"""))
        assertTrue(catalogContent.contains("""retrofit = "2.9.0""""))
    }

    private fun configureGroovyBuildFile() {
        myFixture.configureByText(
            GroovyFileType.GROOVY_FILE_TYPE,
            """
                dependencies {
                    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
                }
            """.trimIndent()
        )
    }

    private fun detectDependency() = PsiTreeUtil.findChildrenOfType(myFixture.file, GrCall::class.java)
        .firstNotNullOfOrNull { DependencyDetector.detectInGroovyDsl(it) }

    private fun catalogFile() = LocalFileSystem.getInstance()
        .findFileByPath("${project.basePath}/gradle/libs.versions.toml")
}
