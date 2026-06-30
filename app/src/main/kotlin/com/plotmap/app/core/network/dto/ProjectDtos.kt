package com.plotmap.app.core.network.dto
import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponse(
    val id: String,
    val title: String,
    val type: String,
    val description: String? = null,
    val createdAt: String,
)

@Serializable
data class CreateProjectRequest(
    val title: String,
    val description: String,
)

@Serializable
data class UpdateProjectRequest(
    val title: String? = null,
    val description: String? = null,
)

@Serializable
data class GenerateProjectRequest(
    val name: String,
    val description: String,
    val text: String,
)

@Serializable
data class JobStatusResponse(
    val jobId: String,
    val projectId: String,
    val status: String,
    val errorMessage: String? = null,
)

@Serializable
data class GraphResponse(
    val id: String,
    val title: String,
    val type: String,
    val description: String? = null,
    val sourceText: String? = null,
    val events: List<EventDto>,
    val connections: List<ConnectionDto>,
    val characters: List<CharacterDto> = emptyList(),
    val createdAt: String,
)

@Serializable
data class ChapterDto(
    val id: String,
    val chapterOrder: Int,
    val title: String? = null,
    val createdAt: String,
)

@Serializable
data class ChapterDetailDto(
    val id: String,
    val chapterOrder: Int,
    val title: String? = null,
    val text: String,
    val createdAt: String,
)

@Serializable
data class AddChapterRequest(
    val title: String? = null,
    val text: String? = null,
)

@Serializable
data class AddChapterResponse(
    val chapter: ChapterDto,
    val job: JobStatusResponse,
)

@Serializable
data class HealthResponse(
    val status: String,
    val message: String? = null,
)

@Serializable
data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val impactLevel: Int,
    val level: Int,
    val orderInLevel: Int,
    val characterIds: List<String> = emptyList(),
    val tagIds: List<String> = emptyList(),
    val customPositionX: Double? = null,
    val customPositionY: Double? = null,
    val color: String? = null,
)

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String? = null,
    val impactLevel: Int? = null,
    val level: Int? = null,
    val orderInLevel: Int? = null,
    val customPositionX: Double? = null,
    val customPositionY: Double? = null,
    val color: String? = null,
    val characterIds: List<String>? = null,
    val tagIds: List<String>? = null,
)

@Serializable
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val suggestedSystemRole: String? = null,
    val impactLevel: Int? = null,
    val status: String? = null,
    val userNotes: String? = null,
    val level: Int? = null,
    val orderInLevel: Int? = null,
    val customPositionX: Double? = null,
    val customPositionY: Double? = null,
    val color: String? = null,
    val characterIds: List<String>? = null,
    val storyArcIds: List<String>? = null,
    val tagIds: List<String>? = null,
)

@Serializable
data class ConnectionDto(
    val id: String? = null,
    val sourceEventId: String,
    val targetEventId: String,
    val type: String,
)

@Serializable
data class CharacterDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val role: String? = null,
    val color: String? = null,
)
