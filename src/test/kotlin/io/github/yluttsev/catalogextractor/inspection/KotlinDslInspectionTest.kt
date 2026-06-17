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
import io.github.yluttsev.catalogextractor.model.DependencyCoordinate
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.model.GradleFormat
import io.github.yluttsev.catalogextractor.quickfix.ExtractToVersionCatalogFix
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.nio.file.Path

class KotlinDslInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        Path.of(requireNotNull(project.basePath), "gradle").toFile().deleteRecursively()
    }

    fun `test reports versioned dependency`() {
        myFixture.configureByText(
            "build.gradle.kts",
            """
                dependencies {
                    implementation("com.squareup.retrofit2:retrofit:2.9.0")
                }
            """.trimIndent()
        )

        val problems = inspectFile(KotlinDslInspection())

        assertEquals(problemDescription(), problems.single().descriptionTemplate)
    }

    fun `test reports versionless dependency`() {
        myFixture.configureByText(
            "build.gradle.kts",
            """
                dependencies {
                    implementation("org.springframework.boot:spring-boot-starter-web")
                }
            """.trimIndent()
        )

        val problems = inspectFile(KotlinDslInspection())

        assertEquals(problemDescription(), problems.single().descriptionTemplate)
    }

    fun `test quick fix extracts dependency to new version catalog`() {
        val buildFile = myFixture.tempDirFixture.createFile(
            "build.gradle.kts",
            """
                dependencies {
                    implementation("com.squareup.retrofit2:retrofit:2.9.0")
                }
            """.trimIndent()
        )
        myFixture.configureFromExistingVirtualFile(buildFile)

        assertEquals(problemDescription(), inspectFile(KotlinDslInspection()).single().descriptionTemplate)

        val info = retrofitDependencyInfo(GradleFormat.KOTLIN_DSL)
        val fix = ExtractToVersionCatalogFix(info)
        val literal = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, KtStringTemplateExpression::class.java))
        fix.applyFix(project, literal)

        myFixture.checkResult(
            """
                dependencies {
                    implementation(libs.retrofit)
                }
            """.trimIndent()
        )

        val catalog = requireNotNull(catalogFile())
        val catalogContent = VfsUtilCore.loadText(catalog)
        assertTrue(catalogContent.contains("""retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }"""))
        assertTrue(catalogContent.contains("""retrofit = "2.9.0""""))
    }

    fun `test quick fix extracts versionless dependency to new version catalog`() {
        val buildFile = myFixture.tempDirFixture.createFile(
            "build.gradle.kts",
            """
                dependencies {
                    implementation("org.springframework.boot:spring-boot-starter-web")
                }
            """.trimIndent()
        )
        myFixture.configureFromExistingVirtualFile(buildFile)

        val info = springBootStarterWebDependencyInfo(GradleFormat.KOTLIN_DSL)
        val fix = ExtractToVersionCatalogFix(info)
        val literal = requireNotNull(PsiTreeUtil.findChildOfType(myFixture.file, KtStringTemplateExpression::class.java))
        fix.applyFix(project, literal)

        myFixture.checkResult(
            """
                dependencies {
                    implementation(libs.spring.boot.starter.web)
                }
            """.trimIndent()
        )

        val catalog = requireNotNull(catalogFile())
        val catalogContent = VfsUtilCore.loadText(catalog)
        assertTrue(catalogContent.contains("""spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }"""))
        assertTrue(!catalogContent.contains("""version.ref = "spring-boot-starter-web""""))
    }

    private fun inspectFile(inspection: KotlinDslInspection): List<ProblemDescriptor> {
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

    private fun retrofitDependencyInfo(format: GradleFormat): DependencyInfo =
        DependencyInfo(
            configuration = "implementation",
            coordinate = DependencyCoordinate(
                groupId = "com.squareup.retrofit2",
                artifactId = "retrofit",
                version = "2.9.0"
            ),
            format = format
        )

    private fun springBootStarterWebDependencyInfo(format: GradleFormat): DependencyInfo =
        DependencyInfo(
            configuration = "implementation",
            coordinate = DependencyCoordinate(
                groupId = "org.springframework.boot",
                artifactId = "spring-boot-starter-web",
                version = null
            ),
            format = format
        )

    private fun catalogFile() = LocalFileSystem.getInstance()
        .findFileByPath("${project.basePath}/gradle/libs.versions.toml")

    private fun problemDescription(): String =
        CatalogExtractorBundle.message("inspection.dependency.problem.description")
}
