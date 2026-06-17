package io.github.yluttsev.catalogextractor.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import io.github.yluttsev.catalogextractor.catalog.AliasGenerator
import io.github.yluttsev.catalogextractor.catalog.VersionCatalogTomlEditor
import io.github.yluttsev.catalogextractor.model.DependencyInfo
import java.nio.file.Files
import java.nio.file.Path

class ExtractToVersionCatalogFix(private val info: DependencyInfo) : LocalQuickFix {

    private val catalogFileService = VersionCatalogFileService()
    private val usageReplacer = GradleDependencyUsageReplacer()
    private val projectRefresher = GradleProjectRefresher()

    override fun getFamilyName(): String = "Extract to version catalog"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        applyFix(project, descriptor.psiElement)
    }

    internal fun applyFix(project: Project, element: PsiElement) {
        val replacementTarget = usageReplacer.prepare(element, info) ?: return
        var shouldRefreshGradle = false

        try {
        WriteCommandAction.runWriteCommandAction(project, "Extract to Version Catalog", null, {
            val existingCatalogFile = catalogFileService.find(project)
            val content = existingCatalogFile
                ?.let { String(it.file.contentsToByteArray(), Charsets.UTF_8) }
                ?: VersionCatalogTomlEditor.createEmptyCatalogContent()

            val alias = VersionCatalogTomlEditor.findExistingAlias(content, info.module)
            if (alias != null) {
                LOG.debug("Using existing version catalog alias '$alias' for ${info.module}")
            }

            var updatedCatalogContent: String? = null
            val resolvedAlias = alias ?: run {
                val newAlias = AliasGenerator.generate(info, VersionCatalogTomlEditor.getAllAliases(content))
                updatedCatalogContent = VersionCatalogTomlEditor.addEntry(content, newAlias, info)
                LOG.debug("Added version catalog alias '$newAlias' for ${info.module}")
                newAlias
            }

            val usageReplaced = usageReplacer.replace(replacementTarget, info, resolvedAlias, project)
            if (usageReplaced) {
                updatedCatalogContent?.let {
                    val catalogFile = existingCatalogFile ?: catalogFileService.create(project) ?: return@runWriteCommandAction
                    val catalogPath = Path.of(catalogFile.file.path)
                    Files.createDirectories(catalogPath.parent)
                    Files.writeString(catalogPath, it, Charsets.UTF_8)
                    catalogFile.file.refresh(false, false)
                }
            }
            shouldRefreshGradle = usageReplaced
        })

        } catch (e: Exception) {
            LOG.error("Failed to extract ${info.module} to version catalog", e)
            return
        }

        if (shouldRefreshGradle) {
            projectRefresher.refresh(project)
        } else {
            LOG.warn("Extract to version catalog quick fix finished without changes for ${info.module}")
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ExtractToVersionCatalogFix::class.java)
    }
}
