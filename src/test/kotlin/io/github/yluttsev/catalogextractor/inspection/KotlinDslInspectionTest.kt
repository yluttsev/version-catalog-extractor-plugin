package io.github.yluttsev.catalogextractor.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.yluttsev.catalogextractor.model.DependencyCoordinate
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import io.github.yluttsev.catalogextractor.model.GradleFormat
import io.github.yluttsev.catalogextractor.quickfix.ExtractToVersionCatalogFix
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class KotlinDslInspectionTest : BasePlatformTestCase() {

    fun `test reports hardcoded dependency version`() {
        myFixture.configureByText(
            "build.gradle.kts",
            """
                dependencies {
                    implementation("com.squareup.retrofit2:retrofit:2.9.0")
                }
            """.trimIndent()
        )

        val problems = inspectFile(KotlinDslInspection())

        assertEquals("Hardcoded dependency version '2.9.0'", problems.single().descriptionTemplate)
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

        assertEquals("Hardcoded dependency version '2.9.0'", inspectFile(KotlinDslInspection()).single().descriptionTemplate)

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

    private fun catalogFile() = LocalFileSystem.getInstance()
        .findFileByPath("${project.basePath}/gradle/libs.versions.toml")
}
