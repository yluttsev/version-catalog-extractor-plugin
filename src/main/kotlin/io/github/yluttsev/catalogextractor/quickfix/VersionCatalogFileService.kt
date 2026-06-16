package io.github.yluttsev.catalogextractor.quickfix

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

class VersionCatalogFileService {

    data class CatalogFile(
        val file: VirtualFile,
        val created: Boolean
    )

    fun find(project: Project): CatalogFile? {
        val baseDir = findBaseDir(project) ?: return null
        val gradleDir = baseDir.findChild(GRADLE_DIR) ?: return null
        val catalog = gradleDir.findChild(CATALOG_FILE) ?: return null
        return CatalogFile(catalog, created = false)
    }

    fun create(project: Project): CatalogFile? {
        val baseDir = findBaseDir(project) ?: return null

        val gradleDir = baseDir.findChild(GRADLE_DIR)
            ?: baseDir.createChildDirectory(null, GRADLE_DIR).also {
                LOG.debug("Created '$GRADLE_DIR' directory for ${project.name}")
            }

        val existingCatalog = gradleDir.findChild(CATALOG_FILE)
        if (existingCatalog != null) {
            return CatalogFile(existingCatalog, created = false)
        }

        val newCatalog = gradleDir.createChildData(null, CATALOG_FILE).also {
            LOG.debug("Created '$GRADLE_DIR/$CATALOG_FILE' for ${project.name}")
        }
        return CatalogFile(newCatalog, created = true)
    }

    fun findOrCreate(project: Project): CatalogFile? = find(project) ?: create(project)

    private fun findBaseDir(project: Project): VirtualFile? {
        val basePath = project.basePath
        if (basePath == null) {
            LOG.warn("Unable to find project base path for ${project.name}")
            return null
        }

        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath)
        if (baseDir == null) {
            LOG.warn("Unable to resolve project base directory '$basePath'")
            return null
        }

        return baseDir
    }

    companion object {
        private const val GRADLE_DIR = "gradle"
        private const val CATALOG_FILE = "libs.versions.toml"
        private val LOG = Logger.getInstance(VersionCatalogFileService::class.java)
    }
}
