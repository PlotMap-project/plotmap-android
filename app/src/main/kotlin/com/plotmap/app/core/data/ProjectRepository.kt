package com.plotmap.app.core.data

import androidx.compose.ui.geometry.Offset
import com.plotmap.app.core.coroutines.DispatcherProvider
import com.plotmap.app.core.models.EVENT_SHORT_DESCRIPTION_MAX
import com.plotmap.app.core.models.EVENT_TAG_PALETTE
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
import com.plotmap.app.core.network.dto.CreateEventRequest
import com.plotmap.app.core.network.dto.CreateProjectRequest
import com.plotmap.app.core.network.dto.EventDto
import com.plotmap.app.core.network.dto.GenerateProjectRequest
import com.plotmap.app.core.network.dto.GraphResponse
import com.plotmap.app.core.network.dto.JobStatusResponse
import com.plotmap.app.core.network.dto.UpdateEventRequest
import com.plotmap.app.core.network.dto.UpdateProjectRequest
import com.plotmap.app.feature.home.HomeProjectItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
    private val dispatchers: DispatcherProvider,
) {
    suspend fun loadProjects(): List<HomeProjectItem> =
        withContext(dispatchers.io) {
            plotMapApi.getProjects().map { response ->
                response.toHomeProjectItem()
            }
        }

    suspend fun renameProject(
        projectId: String,
        title: String,
    ): HomeProjectItem =
        withContext(dispatchers.io) {
            plotMapApi.updateProject(
                id = projectId,
                request = UpdateProjectRequest(title = title),
            ).toHomeProjectItem()
        }

    suspend fun deleteProject(projectId: String) {
        withContext(dispatchers.io) {
            plotMapApi.deleteProject(projectId)
        }
    }

    suspend fun createProject(
        title: String,
        description: String,
    ): HomeProjectItem =
        withContext(dispatchers.io) {
            plotMapApi.createProject(
                CreateProjectRequest(
                    title = title,
                    description = description,
                ),
            ).toHomeProjectItem()
        }

    suspend fun generateGraph(
        title: String,
        description: String,
        text: String,
    ): EditorGraph =
        withContext(dispatchers.io) {
            val job =
                plotMapApi.generateProject(
                    GenerateProjectRequest(
                        name = title,
                        description = description,
                        text = text,
                    ),
                )
            val completed = awaitJobCompletion(job.jobId)
            loadGraph(completed.projectId)
        }

    suspend fun loadChapters(projectId: String): List<ManuscriptChapter> =
        withContext(dispatchers.io) {
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
        }

    suspend fun loadChapterText(
        projectId: String,
        chapterId: String,
    ): ManuscriptChapter =
        withContext(dispatchers.io) {
            val dto = plotMapApi.getChapterById(projectId, chapterId)
            ManuscriptChapter(
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
    ): String =
        withContext(dispatchers.io) {
            plotMapApi.addChapter(projectId, AddChapterRequest(title = title, text = text)).job.jobId
        }

    suspend fun updateChapter(
        projectId: String,
        chapterId: String,
        text: String,
        title: String?,
    ): String =
        withContext(dispatchers.io) {
            plotMapApi.updateChapter(projectId, chapterId, AddChapterRequest(title = title, text = text)).job.jobId
        }

    suspend fun awaitJobCompletion(jobId: String): JobStatusResponse =
        withContext(dispatchers.io) {
            repeat(GENERATION_MAX_POLLS) {
                val job = plotMapApi.getJobStatus(jobId)
                when (job.status.trim().uppercase()) {
                    JOB_STATUS_COMPLETED -> return@withContext job
                    JOB_STATUS_FAILED -> throw GenerationFailedException(job.errorMessage)
                    else -> delay(GENERATION_POLL_INTERVAL_MS)
                }
            }
            throw GenerationFailedException(null)
        }

    suspend fun loadGraph(projectId: String): EditorGraph =
        withContext(dispatchers.io) {
            plotMapApi.getProjectDetails(projectId).toEditorGraph()
        }

    suspend fun saveEventPosition(
        projectId: String,
        eventId: String,
        position: Offset,
    ) {
        withContext(dispatchers.io) {
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
    }

    suspend fun createEvent(
        projectId: String,
        event: EditorEvent,
    ): String =
        withContext(dispatchers.io) {
            plotMapApi.createEvent(
                projectId = projectId,
                request =
                    CreateEventRequest(
                        title = event.title,
                        description = event.description,
                        impactLevel = event.impactLevel,
                        level = event.level,
                        orderInLevel = event.orderInLevel,
                        customPositionX = event.position?.x?.toDouble(),
                        customPositionY = event.position?.y?.toDouble(),
                        color = argbToHex(event.colorArgb),
                        characterIds = event.characterIds,
                        tagIds = event.tagIds,
                    ),
            ).id
        }

    suspend fun saveEventColor(
        projectId: String,
        eventId: String,
        colorArgb: Long?,
    ) {
        withContext(dispatchers.io) {
            plotMapApi.updateEvent(
                projectId = projectId,
                eventId = eventId,
                request = UpdateEventRequest(color = argbToHex(colorArgb) ?: ""),
            )
        }
    }

    suspend fun saveEventLayout(
        projectId: String,
        eventId: String,
        level: Int?,
        orderInLevel: Int?,
        position: Offset?,
    ) {
        withContext(dispatchers.io) {
            plotMapApi.updateEvent(
                projectId = projectId,
                eventId = eventId,
                request =
                    UpdateEventRequest(
                        level = level,
                        orderInLevel = orderInLevel,
                        customPositionX = position?.x?.toDouble(),
                        customPositionY = position?.y?.toDouble(),
                    ),
            )
        }
    }

    suspend fun saveEventContent(
        projectId: String,
        event: EditorEvent,
    ) {
        withContext(dispatchers.io) {
            plotMapApi.updateEvent(
                projectId = projectId,
                eventId = event.id,
                request =
                    UpdateEventRequest(
                        title = event.title,
                        description = event.description,
                        impactLevel = event.impactLevel,
                        characterIds = event.characterIds,
                        tagIds = event.tagIds,
                    ),
            )
        }
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
            tagIds = tagIds,
            colorArgb = parseHexColor(color)?.takeIf { it in EVENT_TAG_PALETTE },
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

    private fun argbToHex(colorArgb: Long?): String? {
        if (colorArgb == null) return null
        return "#%06X".format(colorArgb and 0xFFFFFFL)
    }
}
