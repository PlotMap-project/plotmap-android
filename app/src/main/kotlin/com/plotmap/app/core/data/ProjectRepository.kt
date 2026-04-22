package com.plotmap.app.core.data

import com.plotmap.app.core.network.PlotMapApi
import com.plotmap.app.core.network.dto.CreateProjectRequest
import com.plotmap.app.core.network.dto.GenerateProjectRequest
import com.plotmap.app.core.network.dto.UpdateProjectRequest
import com.plotmap.app.feature.home.HomeProjectItem
import java.time.Instant

class ProjectRepository(
    private val plotMapApi: PlotMapApi,
) {
    suspend fun loadProjects(): List<HomeProjectItem> =
        plotMapApi.getProjects().map { response ->
            response.toHomeProjectItem()
        }

    suspend fun renameProject(
        projectId: String,
        title: String,
    ): HomeProjectItem =
        plotMapApi.updateProject(
            id = projectId,
            request = UpdateProjectRequest(title = title),
        ).toHomeProjectItem()

    suspend fun deleteProject(projectId: String) {
        plotMapApi.deleteProject(projectId)
    }

    suspend fun createProject(
        title: String,
        description: String,
    ): HomeProjectItem =
        plotMapApi.createProject(
            CreateProjectRequest(
                title = title,
                description = description,
            ),
        ).toHomeProjectItem()

    suspend fun generateProject(
        title: String,
        description: String,
        text: String,
    ): HomeProjectItem =
        plotMapApi.generateProject(
            GenerateProjectRequest(
                name = title,
                description = description,
                text = text,
            ),
        ).toHomeProjectItem()

    private fun com.plotmap.app.core.network.dto.ProjectResponse.toHomeProjectItem(): HomeProjectItem =
        HomeProjectItem(
            id = id,
            title = title,
            description = description.orEmpty(),
            isAiGenerated = type == "ai_generated",
            createdAt = createdAt.toEpochMillis(),
            modifiedAt = createdAt.toEpochMillis(),
        )

    private fun String.toHomeProjectItemType(): Boolean = this == "ai_generated"

    private fun com.plotmap.app.core.network.dto.GraphResponse.toHomeProjectItem(): HomeProjectItem =
        HomeProjectItem(
            id = id,
            title = title,
            description = description.orEmpty(),
            isAiGenerated = type.toHomeProjectItemType(),
            createdAt = createdAt.toEpochMillis(),
            modifiedAt = createdAt.toEpochMillis(),
        )

    private fun String.toEpochMillis(): Long =
        runCatching { Instant.parse(this).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())
}
