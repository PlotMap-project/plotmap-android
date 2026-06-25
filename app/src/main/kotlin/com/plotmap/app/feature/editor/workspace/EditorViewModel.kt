package com.plotmap.app.feature.editor.workspace

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plotmap.app.core.data.ProjectRepository
import com.plotmap.app.core.models.CharacterSignificance
import com.plotmap.app.core.models.EditorCharacter
import com.plotmap.app.core.models.EditorConnection
import com.plotmap.app.core.models.EditorConnectionType
import com.plotmap.app.core.models.EditorEvent
import com.plotmap.app.core.models.EditorMode
import com.plotmap.app.core.models.EditorTab
import com.plotmap.app.core.models.EventTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditorUiState(
    val mode: EditorMode = EditorMode.MANUAL,
    val selectedTab: EditorTab = EditorTab.GRAPH,
    val events: List<EditorEvent> = emptyList(),
    val connections: List<EditorConnection> = emptyList(),
    val characters: List<EditorCharacter> = emptyList(),
    val tags: List<EventTag> = emptyList(),
    val selectedEvent: EditorEvent? = null,
    val connectionSourceId: String? = null,
    val scale: Float = 1.0f,
    val offset: Offset = Offset.Zero,
    val minScale: Float = 0.05f,
    val maxScale: Float = 5f,
)

class EditorViewModel(
    private val projectRepository: ProjectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    fun initTags(defaults: List<EventTag>) {
        _uiState.update { state ->
            if (state.tags.isEmpty()) state.copy(tags = defaults) else state
        }
    }

    fun selectTab(tab: EditorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun addEvent(
        title: String,
        shortDescription: String,
        description: String,
        impactLevel: Int,
        eventDate: String,
        characterIds: List<String>,
        tagIds: List<String>,
        position: Offset?,
    ) {
        val newEvent =
            EditorEvent(
                title = title,
                shortDescription = shortDescription,
                description = description,
                impactLevel = impactLevel,
                eventDate = eventDate,
                characterIds = characterIds,
                tagIds = tagIds,
                position = position,
            )
        _uiState.update { it.copy(events = it.events + newEvent) }
    }

    fun updateEvent(
        id: String,
        title: String,
        shortDescription: String,
        description: String,
        impactLevel: Int,
        eventDate: String,
        characterIds: List<String>,
        tagIds: List<String>,
    ) {
        _uiState.update { state ->
            val updatedEvents =
                state.events.map { event ->
                    if (event.id == id) {
                        event.copy(
                            title = title,
                            shortDescription = shortDescription,
                            description = description,
                            impactLevel = impactLevel,
                            eventDate = eventDate,
                            characterIds = characterIds,
                            tagIds = tagIds,
                        )
                    } else {
                        event
                    }
                }
            state.copy(
                events = updatedEvents,
                selectedEvent = updatedEvents.find { it.id == state.selectedEvent?.id },
            )
        }
    }

    fun updateEventColor(
        id: String,
        colorArgb: Long?,
    ) {
        _uiState.update { state ->
            val updatedEvents =
                state.events.map { if (it.id == id) it.copy(colorArgb = colorArgb) else it }
            state.copy(
                events = updatedEvents,
                selectedEvent = updatedEvents.find { it.id == state.selectedEvent?.id },
            )
        }
    }

    fun updateEventPosition(
        id: String,
        dragAmount: Offset,
    ) {
        _uiState.update { state ->
            val updatedEvents =
                state.events.map { event ->
                    if (event.id == id) {
                        val base = event.position ?: Offset.Zero
                        event.copy(position = base + dragAmount, isManuallyPositioned = true)
                    } else {
                        event
                    }
                }
            state.copy(
                events = updatedEvents,
                selectedEvent = updatedEvents.find { it.id == state.selectedEvent?.id },
            )
        }
    }

    fun deleteEvent(id: String) {
        _uiState.update { state ->
            state.copy(
                events = state.events.filter { it.id != id },
                connections = state.connections.filter { it.sourceId != id && it.targetId != id },
                selectedEvent = if (state.selectedEvent?.id == id) null else state.selectedEvent,
            )
        }
    }

    fun selectEvent(event: EditorEvent?) {
        _uiState.update { it.copy(selectedEvent = event) }
    }

    fun addCharacter(
        name: String,
        description: String,
        significance: CharacterSignificance,
    ): String {
        val character =
            EditorCharacter(
                name = name,
                description = description,
                significance = significance,
            )
        _uiState.update { it.copy(characters = it.characters + character) }
        return character.id
    }

    fun updateCharacter(
        id: String,
        name: String,
        description: String,
        significance: CharacterSignificance,
    ) {
        _uiState.update { state ->
            state.copy(
                characters =
                    state.characters.map { character ->
                        if (character.id == id) {
                            character.copy(
                                name = name,
                                description = description,
                                significance = significance,
                            )
                        } else {
                            character
                        }
                    },
            )
        }
    }

    fun deleteCharacter(id: String) {
        _uiState.update { state ->
            state.copy(
                characters = state.characters.filter { it.id != id },
                events = state.events.map { it.copy(characterIds = it.characterIds - id) },
            )
        }
    }

    fun addTag(
        label: String,
        colorArgb: Long,
    ): String {
        val tag =
            EventTag(
                id = UUID.randomUUID().toString(),
                label = label,
                colorArgb = colorArgb,
                isCustom = true,
            )
        _uiState.update { it.copy(tags = it.tags + tag) }
        return tag.id
    }

    fun startConnectionMode(sourceId: String) {
        _uiState.update { it.copy(connectionSourceId = sourceId) }
    }

    fun completeConnection(
        targetId: String,
        type: EditorConnectionType,
        description: String,
    ) {
        _uiState.update { state ->
            val sourceId = state.connectionSourceId
            if (sourceId != null && sourceId != targetId) {
                val newConnection =
                    EditorConnection(
                        sourceId = sourceId,
                        targetId = targetId,
                        type = type,
                        description = description,
                    )
                state.copy(
                    connections = state.connections + newConnection,
                    connectionSourceId = null,
                )
            } else {
                state.copy(connectionSourceId = null)
            }
        }
    }

    fun cancelConnectionMode() {
        _uiState.update { it.copy(connectionSourceId = null) }
    }

    fun deleteConnection(id: String) {
        _uiState.update { state ->
            state.copy(connections = state.connections.filter { it.id != id })
        }
    }

    fun updateTransform(
        zoomDelta: Float,
        panDelta: Offset,
    ) {
        _uiState.update { state ->
            state.copy(
                scale = (state.scale * zoomDelta).coerceIn(state.minScale, state.maxScale),
                offset = state.offset + panDelta,
            )
        }
    }

    fun initScale(
        scale: Float,
        offset: Offset,
        minScale: Float,
        maxScale: Float,
    ) {
        _uiState.update { it.copy(scale = scale, offset = offset, minScale = minScale, maxScale = maxScale) }
    }

    fun autoLayout(density: Density) {
        _uiState.update { state ->
            val nodeMaxPx = with(density) { NODE_MAX_DP.dp.toPx() }
            val stepX = nodeMaxPx * 1.2f
            val stepY = nodeMaxPx * 1.5f
            val startX = with(density) { 32.dp.toPx() }
            val startY = with(density) { 32.dp.toPx() }

            val manualEvents = state.events.filter { it.isManuallyPositioned }
            val autoEvents = state.events.filter { !it.isManuallyPositioned }

            val byDate = autoEvents.groupBy { it.eventDate }
            val sortedDates =
                byDate.keys.sortedWith(
                    compareBy { dateKey -> parseEventDate(dateKey) },
                )

            val laidOut = mutableMapOf<String, EditorEvent>()
            manualEvents.forEach { laidOut[it.id] = it }

            sortedDates.forEachIndexed { layerIndex, date ->
                val layer = byDate[date] ?: return@forEachIndexed
                layer.forEachIndexed { colIndex, event ->
                    laidOut[event.id] =
                        event.copy(
                            position =
                                Offset(
                                    x = startX + colIndex * stepX,
                                    y = startY + layerIndex * stepY,
                                ),
                        )
                }
            }

            state.copy(events = state.events.map { laidOut[it.id] ?: it })
        }
    }

    fun saveProject(projectId: String) {
        viewModelScope.launch {
            runCatching { _uiState.value }
        }
    }

    private fun parseEventDate(dateStr: String): Long {
        if (dateStr.isBlank()) return Long.MAX_VALUE
        val parts = dateStr.split(".")
        if (parts.size != 3) return Long.MAX_VALUE
        return try {
            val d = parts[0].toInt()
            val m = parts[1].toInt()
            val y = parts[2].toInt()
            y * 10000L + m * 100L + d
        } catch (_: NumberFormatException) {
            Long.MAX_VALUE
        }
    }

    private companion object {
        const val NODE_MAX_DP = 172
    }
}
