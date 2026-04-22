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
data class GraphResponse(
    val id: String,
    val title: String,
    val type: String,
    val description: String? = null,
    val sourceText: String? = null,
    val events: List<EventDto>,
    val connections: List<ConnectionDto>,
    val createdAt: String,
)

@Serializable
data class HealthResponse(
    val status: String,
    val message: String,
)

@Serializable
data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val impactLevel: Int,
    val level: Int,
    val orderInLevel: Int,
)

@Serializable
data class ConnectionDto(
    val id: String? = null,
    val sourceEventId: String,
    val targetEventId: String,
    val type: String,
)
