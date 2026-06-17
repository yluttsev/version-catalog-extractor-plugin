package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.yluttsev.catalogextractor.CatalogExtractorBundle
import io.github.yluttsev.catalogextractor.detection.PluginDetector
import io.github.yluttsev.catalogextractor.quickfix.ExtractPluginToVersionCatalogFix
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import java.nio.file.Files
import java.nio.file.Path

class KotlinPluginDslInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        Path.of(requireNotNull(project.basePath), "gradle").toFile().deleteRecursively()
    }

    fun `test detects versioned plugin declaration`() {
        configureKotlinBuildFile("""id("org.springframework.boot") version "3.5.0"""")

        val info = detectPlugin()

        assertNotNull(info)
        assertEquals("org.springframework.boot", info?.pluginId)
        assertEquals("3.5.0", info?.version)
    }

    fun `test ignores core plugin declaration`() {
        configureKotlinBuildFile("""id("java")""")

        assertNull(detectPlugin())
    }

    fun `test ignores plugin-like expression outside plugins block`() {
        myFixture.configureByText(
            "build.gradle.kts",
            """
                val plugin = id("org.springframework.boot") version "3.5.0"
            """.trimIndent()
        )

        assertNull(detectPlugin())
    }

    fun `test reports versioned plugin declaration`() {
        configureKotlinBuildFile("""id("org.springframework.boot") version "3.5.0"""")

        val problems = inspectFile(KotlinPluginDslInspection())

        assertEquals(problemDescription(), problems.single().descriptionTemplate)
    }

    fun `test quick fix extracts plugin to new version catalog`() {
        val buildFile = myFixture.tempDirFixture.createFile(
            "build.gradle.kts",
            """
                plugins {
                    id("org.springframework.boot") version "3.5.0"
                }
            """.trimIndent()
        )
        myFixture.configureFromExistingVirtualFile(buildFile)

        val info = requireNotNull(detectPlugin())
        val expression = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, KtBinaryExpression::class.java))
        ExtractPluginToVersionCatalogFix(info).applyFix(project, expression)

        myFixture.checkResult(
            """
                plugins {
                    alias(libs.plugins.spring.boot)
                }
            """.trimIndent()
        )

        val catalog = requireNotNull(catalogFile())
        val catalogContent = VfsUtilCore.loadText(catalog)
        assertTrue(catalogContent.contains("""spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }"""))
        assertTrue(catalogContent.contains("""spring-boot = "3.5.0""""))
    }

    fun `test quick fix reuses existing plugin alias`() {
        val catalogPath = Path.of(requireNotNull(project.basePath), "gradle", "libs.versions.toml")
        Files.createDirectories(catalogPath.parent)
        Files.writeString(
            catalogPath,
            """
                [plugins]
                boot = { id = "org.springframework.boot", version.ref = "spring-boot" }

                [versions]
                spring-boot = "3.5.0"
            """.trimIndent()
        )
        val catalog = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(catalogPath))
        myFixture.tempDirFixture.createFile(
            "build.gradle.kts",
            """
                plugins {
                    id("org.springframework.boot") version "3.5.0"
                }
            """.trimIndent()
        )
        myFixture.configureByFile("build.gradle.kts")

        val info = requireNotNull(detectPlugin())
        val expression = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, KtBinaryExpression::class.java))
        ExtractPluginToVersionCatalogFix(info).applyFix(project, expression)

        myFixture.checkResult(
            """
                plugins {
                    alias(libs.plugins.boot)
                }
            """.trimIndent()
        )
        assertEquals(
            """
                [plugins]
                boot = { id = "org.springframework.boot", version.ref = "spring-boot" }

                [versions]
                spring-boot = "3.5.0"
            """.trimIndent(),
            VfsUtilCore.loadText(catalog)
        )
    }

    private fun configureKotlinBuildFile(pluginDeclaration: String) {
        myFixture.configureByText(
            "build.gradle.kts",
            """
                plugins {
                    $pluginDeclaration
                }
            """.trimIndent()
        )
    }

    private fun detectPlugin() = PsiTreeUtil.findChildrenOfType(myFixture.file, KtBinaryExpression::class.java)
        .firstNotNullOfOrNull { PluginDetector.detectInKotlinDsl(it) }

    private fun inspectFile(inspection: KotlinPluginDslInspection): List<ProblemDescriptor> {
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, false)
        val visitor = inspection.buildVisitor(holder, false)
        visitEachElement(myFixture.file, visitor)
        return holder.results
    }

    private fun visitEachElement(file: PsiFile, visitor: com.intellij.psi.PsiElementVisitor) {
        PsiTreeUtil.processElements(file) { element ->
            element.accept(visitor)
            true
        }
    }

    private fun catalogFile() = LocalFileSystem.getInstance()
        .findFileByPath("${project.basePath}/gradle/libs.versions.toml")

    private fun problemDescription(): String =
        CatalogExtractorBundle.message("inspection.plugin.problem.description")
}
