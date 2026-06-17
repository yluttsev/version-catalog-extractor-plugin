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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import java.nio.file.Path

class GroovyDslDependencyDetectorTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        Path.of(requireNotNull(project.basePath), "gradle").toFile().deleteRecursively()
    }

    fun `test detects versioned dependency`() {
        configureGroovyBuildFile()

        val info = detectDependency()

        assertNotNull(info)
        assertEquals("implementation", info?.configuration)
        assertEquals("com.squareup.retrofit2", info?.groupId)
        assertEquals("retrofit", info?.artifactId)
        assertEquals("2.9.0", info?.version)
    }

    fun `test detects versionless dependency`() {
        configureGroovyBuildFile("implementation 'org.springframework.boot:spring-boot-starter-web'")

        val info = detectDependency()

        assertNotNull(info)
        assertEquals("implementation", info?.configuration)
        assertEquals("org.springframework.boot", info?.groupId)
        assertEquals("spring-boot-starter-web", info?.artifactId)
        assertNull(info?.version)
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

    fun `test quick fix extracts versionless dependency to new version catalog`() {
        configureGroovyBuildFile("implementation 'org.springframework.boot:spring-boot-starter-web'")

        val info = requireNotNull(detectDependency())
        val literal = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, GrLiteral::class.java))
        ExtractToVersionCatalogFix(info).applyFix(project, literal)

        myFixture.checkResult(
            """
                dependencies {
                    implementation libs.spring.boot.starter.web
                }
            """.trimIndent()
        )

        val catalog = requireNotNull(catalogFile())
        val catalogContent = VfsUtilCore.loadText(catalog)
        assertTrue(catalogContent.contains("""spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }"""))
        assertTrue(!catalogContent.contains("""version.ref = "spring-boot-starter-web""""))
    }

    private fun configureGroovyBuildFile(dependencyDeclaration: String = "implementation 'com.squareup.retrofit2:retrofit:2.9.0'") {
        myFixture.configureByText(
            GroovyFileType.GROOVY_FILE_TYPE,
            """
                dependencies {
                    $dependencyDeclaration
                }
            """.trimIndent()
        )
    }

    private fun detectDependency() = PsiTreeUtil.findChildrenOfType(myFixture.file, GrCall::class.java)
        .firstNotNullOfOrNull { DependencyDetector.detectInGroovyDsl(it) }

    private fun catalogFile() = LocalFileSystem.getInstance()
        .findFileByPath("${project.basePath}/gradle/libs.versions.toml")
}
