package com.plotmap.app.core.data

import androidx.compose.ui.geometry.Offset
import com.plotmap.app.core.models.EVENT_SHORT_DESCRIPTION_MAX
import com.plotmap.app.core.models.EditorCharacter
import com.plotmap.app.core.models.EditorConnection
import com.plotmap.app.core.models.EditorEvent
import com.plotmap.app.core.models.EditorGraph
import com.plotmap.app.core.models.ManuscriptChapter
import com.plotmap.app.core.models.characterSignificanceFromApi
import com.plotmap.app.core.models.editorConnectionTypeFromApi
import com.plotmap.app.core.network.PlotMapApi
import com.plotmap.app.core.network.dto.AddChapterRequest
import com.plotmap.app.core.network.dto.CharacterDto
import com.plotmap.app.core.network.dto.ConnectionDto
import com.plotmap.app.core.network.dto.CreateProjectRequest
import com.plotmap.app.core.network.dto.EventDto
import com.plotmap.app.core.network.dto.GenerateProjectRequest
import com.plotmap.app.core.network.dto.GraphResponse
import com.plotmap.app.core.network.dto.JobStatusResponse
import com.plotmap.app.core.network.dto.UpdateEventRequest
import com.plotmap.app.core.network.dto.UpdateProjectRequest
import com.plotmap.app.feature.home.HomeProjectItem
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.UUID

private const val GENERATION_POLL_INTERVAL_MS = 5000L
private const val GENERATION_MAX_POLLS = 150
private const val JOB_STATUS_COMPLETED = "COMPLETED"
private const val JOB_STATUS_FAILED = "FAILED"

class GenerationFailedException(
    val reason: String?,
) : Exception(reason)

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

    suspend fun generateGraph(
        title: String,
        description: String,
        text: String,
    ): EditorGraph {
        val job =
            plotMapApi.generateProject(
                GenerateProjectRequest(
                    name = title,
                    description = description,
                    text = text,
                ),
            )
        val completed = awaitJobCompletion(job.jobId)
        return loadGraph(completed.projectId)
    }

    suspend fun loadChapters(projectId: String): List<ManuscriptChapter> =
        plotMapApi.getChapters(projectId)
            .sortedBy { it.chapterOrder }
            .map { dto ->
                ManuscriptChapter(
                    id = dto.id,
                    order = dto.chapterOrder,
                    title = dto.title,
                    locked = true,
                    loaded = false,
                    serverBacked = true,
                )
            }

    suspend fun loadChapterText(
        projectId: String,
        chapterId: String,
    ): ManuscriptChapter {
        val dto = plotMapApi.getChapterById(projectId, chapterId)
        return ManuscriptChapter(
            id = dto.id,
            order = dto.chapterOrder,
            title = dto.title,
            text = dto.text,
            bold = List(dto.text.length) { false },
            italic = List(dto.text.length) { false },
            locked = true,
            loaded = true,
            serverBacked = true,
        )
    }

    suspend fun addChapter(
        projectId: String,
        text: String,
        title: String?,
    ): String {
        val response = plotMapApi.addChapter(projectId, AddChapterRequest(title = title, text = text))
        return response.job.jobId
    }

    suspend fun updateChapter(
        projectId: String,
        chapterId: String,
        text: String,
        title: String?,
    ): String {
        val response = plotMapApi.updateChapter(projectId, chapterId, AddChapterRequest(title = title, text = text))
        return response.job.jobId
    }

    suspend fun awaitJobCompletion(jobId: String): JobStatusResponse {
        repeat(GENERATION_MAX_POLLS) {
            val job = plotMapApi.getJobStatus(jobId)
            when (job.status.trim().uppercase()) {
                JOB_STATUS_COMPLETED -> return job
                JOB_STATUS_FAILED -> throw GenerationFailedException(job.errorMessage)
                else -> delay(GENERATION_POLL_INTERVAL_MS)
            }
        }
        throw GenerationFailedException(null)
    }

    suspend fun loadGraph(projectId: String): EditorGraph = plotMapApi.getProjectDetails(projectId).toEditorGraph()

    suspend fun saveEventPosition(
        projectId: String,
        eventId: String,
        position: Offset,
    ) {
        plotMapApi.updateEvent(
            projectId = projectId,
            eventId = eventId,
            request =
                UpdateEventRequest(
                    customPositionX = position.x.toDouble(),
                    customPositionY = position.y.toDouble(),
                ),
        )
    }

    private fun GraphResponse.toEditorGraph(): EditorGraph =
        EditorGraph(
            projectId = id,
            title = title,
            events = events.map { it.toEditorEvent() },
            connections = connections.map { it.toEditorConnection() },
            characters = characters.map { it.toEditorCharacter() },
            description = description.orEmpty(),
            sourceText = sourceText.orEmpty(),
        )

    private fun EventDto.toEditorEvent(): EditorEvent {
        val hasCustomPosition = customPositionX != null && customPositionY != null
        return EditorEvent(
            id = id,
            title = title,
            shortDescription = description.take(EVENT_SHORT_DESCRIPTION_MAX),
            description = description,
            impactLevel = impactLevel,
            characterIds = characterIds,
            level = level,
            orderInLevel = orderInLevel,
            position = if (hasCustomPosition) Offset(customPositionX!!.toFloat(), customPositionY!!.toFloat()) else null,
            isManuallyPositioned = hasCustomPosition,
        )
    }

    private fun CharacterDto.toEditorCharacter(): EditorCharacter =
        EditorCharacter(
            id = id,
            name = name,
            description = description.orEmpty(),
            significance = characterSignificanceFromApi(role),
            colorArgb = parseHexColor(color),
        )

    private fun ConnectionDto.toEditorConnection(): EditorConnection =
        EditorConnection(
            id = id ?: UUID.randomUUID().toString(),
            sourceId = sourceEventId,
            targetId = targetEventId,
            type = editorConnectionTypeFromApi(type),
        )

    private fun com.plotmap.app.core.network.dto.ProjectResponse.toHomeProjectItem(): HomeProjectItem =
        HomeProjectItem(
            id = id,
            title = title,
            description = description.orEmpty(),
            isAiGenerated = type.equals("ai_generated", ignoreCase = true),
            createdAt = createdAt.toEpochMillis(),
            modifiedAt = createdAt.toEpochMillis(),
        )

    private fun String.toEpochMillis(): Long =
        runCatching { Instant.parse(this).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())

    private fun parseHexColor(hex: String?): Long? {
        val cleaned = hex?.trim()?.removePrefix("#")?.takeIf { it.isNotEmpty() } ?: return null
        val value = cleaned.toLongOrNull(16) ?: return null
        return if (cleaned.length <= 6) value or 0xFF000000L else value
    }
}
