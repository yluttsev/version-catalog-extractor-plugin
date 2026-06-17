package io.github.yluttsev.catalogextractor.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import io.github.yluttsev.catalogextractor.catalog.PluginAliasGenerator
import io.github.yluttsev.catalogextractor.catalog.VersionCatalogTomlEditor
import io.github.yluttsev.catalogextractor.model.PluginInfo
import java.nio.file.Files
import java.nio.file.Path

class ExtractPluginToVersionCatalogFix(private val info: PluginInfo) : LocalQuickFix {

    private val catalogFileService = VersionCatalogFileService()
    private val usageReplacer = GradlePluginUsageReplacer()
    private val projectRefresher = GradleProjectRefresher()

    override fun getFamilyName(): String = "Extract plugin to version catalog"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        applyFix(project, descriptor.psiElement)
    }

    internal fun applyFix(project: Project, element: PsiElement) {
        val replacementTarget = usageReplacer.prepare(element, info) ?: return
        var shouldRefreshGradle = false

        try {
            WriteCommandAction.runWriteCommandAction(project, "Extract Plugin to Version Catalog", null, {
                val existingCatalogFile = catalogFileService.find(project)
                val content = existingCatalogFile
                    ?.let { Files.readString(Path.of(it.file.path), Charsets.UTF_8) }
                    ?: VersionCatalogTomlEditor.createEmptyCatalogContent()

                val alias = VersionCatalogTomlEditor.findExistingPluginAlias(content, info.pluginId)
                if (alias != null) {
                    LOG.debug("Using existing version catalog plugin alias '$alias' for ${info.pluginId}")
                }

                var updatedCatalogContent: String? = null
                val resolvedAlias = alias ?: run {
                    val newAlias = PluginAliasGenerator.generate(info, VersionCatalogTomlEditor.getAllPluginAliases(content))
                    updatedCatalogContent = VersionCatalogTomlEditor.addPluginEntry(content, newAlias, info)
                    LOG.debug("Added version catalog plugin alias '$newAlias' for ${info.pluginId}")
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
            LOG.error("Failed to extract ${info.pluginId} to version catalog", e)
            return
        }

        if (shouldRefreshGradle) {
            projectRefresher.refresh(project)
        } else {
            LOG.warn("Extract plugin to version catalog quick fix finished without changes for ${info.pluginId}")
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ExtractPluginToVersionCatalogFix::class.java)
    }
}
