package io.github.yluttsev.catalogextractor.quickfix

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project

class GradleProjectRefresher {

    fun refresh(project: Project) {
        try {
            ExternalSystemUtil.refreshProjects(ImportSpecBuilder(project, ProjectSystemId("GRADLE")))
            LOG.debug("Requested Gradle project refresh for ${project.name}")
        } catch (exception: RuntimeException) {
            LOG.warn("Unable to refresh Gradle project ${project.name}", exception)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(GradleProjectRefresher::class.java)
    }
}
