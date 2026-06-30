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
import com.plotmap.app.core.models.ManuscriptAlign
import com.plotmap.app.core.models.ManuscriptChapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val manuscriptTitle: String = "",
    val manuscriptDescription: String = "",
    val chapters: List<ManuscriptChapter> = listOf(ManuscriptChapter()),
    val openChapterId: String? = null,
    val isChapterLoading: Boolean = false,
    val isChapterSaving: Boolean = false,
    val chapterSaved: Boolean = false,
    val chapterError: String? = null,
    val isGraphUpdating: Boolean = false,
    val graphUpdateError: String? = null,
    val scale: Float = 1.0f,
    val offset: Offset = Offset.Zero,
    val minScale: Float = 0.05f,
    val maxScale: Float = 5f,
    val isLoading: Boolean = false,
    val loadError: String? = null,
)

class EditorViewModel(
    private val projectRepository: ProjectRepository,
    private val appScope: CoroutineScope,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _exitEvent = MutableSharedFlow<Unit>()
    val exitEvent = _exitEvent.asSharedFlow()

    private var graphRequested = false
    private val savedChapterIds = mutableSetOf<String>()
    private val dirtyChapterIds = mutableSetOf<String>()
    private val persistedEventIds = mutableSetOf<String>()

    fun setMode(mode: EditorMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun loadGraph(projectId: String) {
        if (graphRequested) return
        graphRequested = true
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            runCatching {
                val graph = projectRepository.loadGraph(projectId)
                val chapters = projectRepository.loadChapters(projectId)
                graph to chapters
            }
                .onSuccess { (graph, chapters) ->
                    persistedEventIds.clear()
                    persistedEventIds.addAll(graph.events.map { event -> event.id })
                    _uiState.update {
                        it.copy(
                            mode = EditorMode.AI_READONLY,
                            events = graph.events,
                            connections = graph.connections,
                            characters = graph.characters,
                            manuscriptTitle = graph.title,
                            manuscriptDescription = graph.description,
                            chapters = chapters,
                            openChapterId = null,
                            isLoading = false,
                            loadError = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, loadError = throwable.message) }
                }
        }
    }

    fun retryLoadGraph(projectId: String) {
        graphRequested = false
        loadGraph(projectId)
    }

    fun loadManualGraph(projectId: String) {
        if (graphRequested) return
        graphRequested = true
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            runCatching { projectRepository.loadGraph(projectId) }
                .onSuccess { graph ->
                    persistedEventIds.clear()
                    persistedEventIds.addAll(graph.events.map { event -> event.id })
                    _uiState.update {
                        it.copy(
                            events = graph.events,
                            connections = graph.connections,
                            characters = graph.characters,
                            isLoading = false,
                            loadError = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, loadError = throwable.message) }
                }
        }
    }

    fun retryLoadManualGraph(projectId: String) {
        graphRequested = false
        loadManualGraph(projectId)
    }

    fun initTags(defaults: List<EventTag>) {
        _uiState.update { state ->
            if (state.tags.isEmpty()) state.copy(tags = defaults) else state
        }
    }

    fun selectTab(tab: EditorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateManuscriptTitle(value: String) {
        _uiState.update { it.copy(manuscriptTitle = value) }
    }

    fun updateManuscriptDescription(value: String) {
        _uiState.update { it.copy(manuscriptDescription = value) }
    }

    fun openChapter(
        projectId: String,
        id: String?,
    ) {
        val previousId = _uiState.value.openChapterId
        if (previousId != null && previousId != id && previousId in dirtyChapterIds) {
            commitChapter(projectId, previousId)
        }
        _uiState.update { state ->
            val relocked =
                if (previousId != null && previousId != id) {
                    state.chapters.map { if (it.id == previousId && it.serverBacked) it.copy(locked = true) else it }
                } else {
                    state.chapters
                }
            state.copy(
                chapters = relocked,
                openChapterId = id,
                chapterError = null,
                chapterSaved = id != null && id in savedChapterIds,
            )
        }
        if (id == null) return
        val chapter = _uiState.value.chapters.find { it.id == id } ?: return
        if (chapter.loaded || !chapter.serverBacked) return
        viewModelScope.launch {
            _uiState.update { it.copy(isChapterLoading = true, chapterError = null) }
            runCatching { projectRepository.loadChapterText(projectId, id) }
                .onSuccess { full ->
                    _uiState.update { state ->
                        state.copy(
                            chapters = state.chapters.map { if (it.id == id) full else it },
                            isChapterLoading = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isChapterLoading = false, chapterError = throwable.message) }
                }
        }
    }

    fun addChapter() {
        val nextOrder = (_uiState.value.chapters.maxOfOrNull { it.order } ?: 0) + 1
        val chapter = ManuscriptChapter(order = nextOrder, locked = false, loaded = true)
        _uiState.update {
            it.copy(chapters = it.chapters + chapter, openChapterId = chapter.id, chapterSaved = false, chapterError = null)
        }
    }

    fun beginEditChapter(id: String) {
        _uiState.update { state ->
            state.copy(
                chapters = state.chapters.map { if (it.id == id) it.copy(locked = false) else it },
                chapterSaved = false,
            )
        }
    }

    fun saveChapter(
        projectId: String,
        id: String,
    ) {
        val chapter = _uiState.value.chapters.find { it.id == id } ?: return
        if (chapter.text.isBlank() || _uiState.value.isChapterSaving || _uiState.value.chapterSaved) return
        dirtyChapterIds.remove(id)
        viewModelScope.launch {
            _uiState.update { it.copy(isChapterSaving = true, chapterError = null) }
            runCatching { pushChapter(projectId, chapter) }
                .onSuccess { jobId ->
                    savedChapterIds.add(id)
                    _uiState.update {
                        it.copy(
                            isChapterSaving = false,
                            chapterSaved = true,
                            isGraphUpdating = true,
                            graphUpdateError = null,
                        )
                    }
                    updateGraphInBackground(projectId, jobId)
                }
                .onFailure { throwable ->
                    dirtyChapterIds.add(id)
                    _uiState.update { it.copy(isChapterSaving = false, chapterError = throwable.message) }
                }
        }
    }

    private fun commitChapter(
        projectId: String,
        id: String,
    ) {
        val chapter = _uiState.value.chapters.find { it.id == id } ?: return
        if (chapter.text.isBlank()) return
        dirtyChapterIds.remove(id)
        viewModelScope.launch {
            _uiState.update { it.copy(isGraphUpdating = true, graphUpdateError = null) }
            runCatching { pushChapter(projectId, chapter) }
                .onSuccess { jobId ->
                    savedChapterIds.add(id)
                    _uiState.update { it.copy(chapterSaved = it.openChapterId == id) }
                    updateGraphInBackground(projectId, jobId)
                }
                .onFailure { throwable ->
                    dirtyChapterIds.add(id)
                    _uiState.update { it.copy(isGraphUpdating = false, graphUpdateError = throwable.message) }
                }
        }
    }

    fun exitEditor(projectId: String) {
        val openId = _uiState.value.openChapterId
        if (openId != null && openId in dirtyChapterIds) {
            val chapter = _uiState.value.chapters.find { it.id == openId }
            if (chapter != null && chapter.text.isNotBlank()) {
                dirtyChapterIds.remove(openId)
                appScope.launch { runCatching { pushChapter(projectId, chapter) } }
            }
        }
        viewModelScope.launch { _exitEvent.emit(Unit) }
    }

    private suspend fun pushChapter(
        projectId: String,
        chapter: ManuscriptChapter,
    ): String =
        if (chapter.serverBacked) {
            projectRepository.updateChapter(projectId, chapter.id, chapter.text, chapter.title)
        } else {
            projectRepository.addChapter(projectId, chapter.text, chapter.title)
        }

    private fun updateGraphInBackground(
        projectId: String,
        jobId: String,
    ) {
        viewModelScope.launch {
            runCatching {
                projectRepository.awaitJobCompletion(jobId)
                projectRepository.loadGraph(projectId)
            }
                .onSuccess { graph ->
                    persistedEventIds.clear()
                    persistedEventIds.addAll(graph.events.map { event -> event.id })
                    _uiState.update {
                        it.copy(
                            events = graph.events,
                            connections = graph.connections,
                            characters = graph.characters,
                            manuscriptTitle = graph.title,
                            manuscriptDescription = graph.description,
                            isGraphUpdating = false,
                            graphUpdateError = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isGraphUpdating = false, graphUpdateError = throwable.message) }
                }
        }
    }

    fun updateChapterText(
        id: String,
        text: String,
        bold: List<Boolean>,
        italic: List<Boolean>,
    ) {
        val target = _uiState.value.chapters.find { it.id == id } ?: return
        if (target.locked) return
        if (text != target.text) dirtyChapterIds.add(id)
        _uiState.update { state ->
            state.copy(
                chapters =
                    state.chapters.map { chapter ->
                        if (chapter.id == id) chapter.copy(text = text, bold = bold, italic = italic) else chapter
                    },
                chapterSaved = if (text != target.text && state.openChapterId == id) false else state.chapterSaved,
            )
        }
    }

    fun setChapterAlign(
        id: String,
        align: ManuscriptAlign,
    ) {
        _uiState.update { state ->
            state.copy(
                chapters = state.chapters.map { if (it.id == id) it.copy(align = align) else it },
            )
        }
    }

    fun addEvent(
        projectId: String,
        title: String,
        shortDescription: String,
        description: String,
        impactLevel: Int,
        eventDate: String,
        characterIds: List<String>,
        tagIds: List<String>,
        density: Density,
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
                position = null,
            )
        _uiState.update { it.copy(events = it.events + newEvent) }
        autoLayout(density)
        if (_uiState.value.mode == EditorMode.MANUAL) {
            persistNewEvent(projectId, newEvent.id)
        }
    }

    fun updateEvent(
        projectId: String,
        id: String,
        title: String,
        shortDescription: String,
        description: String,
        impactLevel: Int,
        eventDate: String,
        characterIds: List<String>,
        tagIds: List<String>,
        density: Density,
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
        autoLayout(density)
        if (_uiState.value.mode == EditorMode.MANUAL && id in persistedEventIds) {
            _uiState.value.events.find { it.id == id }?.let { event ->
                viewModelScope.launch { runCatching { projectRepository.saveEventContent(projectId, event) } }
            }
            persistManualLayout(projectId, excludeId = null)
        }
    }

    fun updateEventColor(
        projectId: String,
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
        if (id in persistedEventIds) {
            viewModelScope.launch { runCatching { projectRepository.saveEventColor(projectId, id, colorArgb) } }
        }
    }

    private fun persistNewEvent(
        projectId: String,
        tempId: String,
    ) {
        val event = _uiState.value.events.find { it.id == tempId } ?: return
        viewModelScope.launch {
            runCatching { projectRepository.createEvent(projectId, event) }
                .onSuccess { serverId ->
                    persistedEventIds.add(serverId)
                    _uiState.update { state ->
                        state.copy(
                            events = state.events.map { if (it.id == tempId) it.copy(id = serverId) else it },
                            selectedEvent =
                                state.selectedEvent?.let { if (it.id == tempId) it.copy(id = serverId) else it },
                        )
                    }
                    persistManualLayout(projectId, excludeId = null)
                }
        }
    }

    private fun persistManualLayout(
        projectId: String,
        excludeId: String?,
    ) {
        _uiState.value.events
            .filter { it.id in persistedEventIds && it.id != excludeId }
            .forEach { event ->
                viewModelScope.launch {
                    runCatching {
                        projectRepository.saveEventLayout(
                            projectId = projectId,
                            eventId = event.id,
                            level = event.level,
                            orderInLevel = event.orderInLevel,
                            position = event.position,
                        )
                    }
                }
            }
    }

    fun updateConnectionColor(
        id: String,
        colorArgb: Long?,
    ) {
        _uiState.update { state ->
            state.copy(
                connections = state.connections.map { if (it.id == id) it.copy(colorArgb = colorArgb) else it },
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

    fun persistEventPosition(
        projectId: String,
        eventId: String,
    ) {
        if (eventId !in persistedEventIds) return
        val position = _uiState.value.events.find { it.id == eventId }?.position ?: return
        viewModelScope.launch {
            runCatching { projectRepository.saveEventPosition(projectId, eventId, position) }
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
        centroid: Offset,
        pivot: Offset,
    ) {
        _uiState.update { state ->
            val newScale = (state.scale * zoomDelta).coerceIn(state.minScale, state.maxScale)
            val appliedZoom = newScale / state.scale
            val focal = centroid - pivot
            state.copy(
                scale = newScale,
                offset = focal * (1f - appliedZoom) + state.offset * appliedZoom + panDelta,
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
            val orderByDate = state.mode == EditorMode.MANUAL

            val layers: List<List<EditorEvent>> =
                if (!orderByDate) {
                    autoEvents
                        .groupBy { it.level ?: Int.MAX_VALUE }
                        .toSortedMap()
                        .values
                        .map { layer -> layer.sortedBy { it.orderInLevel ?: Int.MAX_VALUE } }
                        .toList()
                } else {
                    val byDate = autoEvents.groupBy { it.eventDate }
                    byDate.keys
                        .sortedWith(compareBy { dateKey -> parseEventDate(dateKey) })
                        .map { date -> byDate[date].orEmpty() }
                }

            val laidOut = mutableMapOf<String, EditorEvent>()
            manualEvents.forEach { laidOut[it.id] = it }

            layers.forEachIndexed { layerIndex, layer ->
                layer.forEachIndexed { colIndex, event ->
                    laidOut[event.id] =
                        event.copy(
                            position =
                                Offset(
                                    x = startX + colIndex * stepX,
                                    y = startY + layerIndex * stepY,
                                ),
                            level = if (orderByDate) layerIndex else event.level,
                            orderInLevel = if (orderByDate) colIndex else event.orderInLevel,
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
