package com.plotmap.app.feature.editor.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.Gold
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.Lavender
import com.plotmap.app.core.designsystem.OnGold
import com.plotmap.app.core.designsystem.components.ManuscriptBackground
import com.plotmap.app.core.designsystem.components.PlotMapBackButton
import com.plotmap.app.core.models.DEFAULT_TAG_SPECS
import com.plotmap.app.core.models.EVENT_TAG_PALETTE
import com.plotmap.app.core.models.EditorCharacter
import com.plotmap.app.core.models.EditorEvent
import com.plotmap.app.core.models.EditorMode
import com.plotmap.app.core.models.EditorTab
import com.plotmap.app.core.models.EventTag
import com.plotmap.app.feature.editor.workspace.components.AddEventDialog
import com.plotmap.app.feature.editor.workspace.components.CharacterDialog
import com.plotmap.app.feature.editor.workspace.components.CharactersTab
import com.plotmap.app.feature.editor.workspace.components.ConnectionDialog
import com.plotmap.app.feature.editor.workspace.components.GraphCanvas
import com.plotmap.app.feature.editor.workspace.components.ManuscriptTab
import com.plotmap.app.feature.editor.workspace.components.connectionTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: String,
    mode: EditorMode,
    viewModel: EditorViewModel,
    onBackToHome: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val defaultTags = rememberDefaultEventTags()
    val density = LocalDensity.current
    val isAiReadOnly = state.mode == EditorMode.AI_READONLY

    LaunchedEffect(Unit) {
        viewModel.initTags(defaultTags)
        viewModel.setMode(mode)
        if (mode == EditorMode.AI_READONLY) {
            viewModel.loadGraph(projectId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exitEvent.collect { onBackToHome() }
    }

    BackHandler { viewModel.exitEditor(projectId) }

    LaunchedEffect(state.events) {
        if (!state.isLoading && state.mode == EditorMode.AI_READONLY && state.events.any { it.position == null }) {
            viewModel.autoLayout(density)
        }
    }

    var showEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EditorEvent?>(null) }
    var showCharacterDialog by remember { mutableStateOf(false) }
    var editingCharacter by remember { mutableStateOf<EditorCharacter?>(null) }
    var showConnectionDialog by remember { mutableStateOf(false) }
    var pendingTarget by remember { mutableStateOf<EditorEvent?>(null) }

    ManuscriptBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.editor_screen_title)) },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = GoldBright,
                        ),
                    navigationIcon = {
                        PlotMapBackButton(
                            onClick = { viewModel.exitEditor(projectId) },
                            contentDescription = stringResource(R.string.back_to_home),
                        )
                    },
                )
            },
            floatingActionButton = {
                val hideFab =
                    state.selectedTab == EditorTab.MANUSCRIPT ||
                        (isAiReadOnly && state.selectedTab == EditorTab.GRAPH)
                if (state.connectionSourceId == null && !hideFab) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (state.selectedTab == EditorTab.GRAPH) {
                                editingEvent = null
                                showEventDialog = true
                            } else {
                                editingCharacter = null
                                showCharacterDialog = true
                            }
                        },
                        containerColor = Gold,
                        contentColor = OnGold,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_event))
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex =
                            when (state.selectedTab) {
                                EditorTab.GRAPH -> 0
                                EditorTab.CHARACTERS -> 1
                                EditorTab.MANUSCRIPT -> 2
                            },
                        containerColor = Color.Transparent,
                        contentColor = GoldBright,
                    ) {
                        Tab(
                            selected = state.selectedTab == EditorTab.GRAPH,
                            onClick = { viewModel.selectTab(EditorTab.GRAPH) },
                            selectedContentColor = GoldBright,
                            unselectedContentColor = Lavender,
                            text = { Text(stringResource(R.string.editor_tab_graph)) },
                        )
                        Tab(
                            selected = state.selectedTab == EditorTab.CHARACTERS,
                            onClick = { viewModel.selectTab(EditorTab.CHARACTERS) },
                            selectedContentColor = GoldBright,
                            unselectedContentColor = Lavender,
                            text = { Text(stringResource(R.string.editor_tab_characters)) },
                        )
                        Tab(
                            selected = state.selectedTab == EditorTab.MANUSCRIPT,
                            onClick = { viewModel.selectTab(EditorTab.MANUSCRIPT) },
                            selectedContentColor = GoldBright,
                            unselectedContentColor = Lavender,
                            text = { Text(stringResource(R.string.editor_tab_manuscript)) },
                        )
                    }

                    when (state.selectedTab) {
                        EditorTab.GRAPH ->
                            when {
                                state.isLoading ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }

                                state.loadError != null ->
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.editor_load_error),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { viewModel.retryLoadGraph(projectId) }) {
                                            Text(stringResource(R.string.editor_retry))
                                        }
                                    }

                                state.isGraphUpdating ->
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = stringResource(R.string.graph_updating_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldBright,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.graph_updating_hint),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                else ->
                                    GraphCanvas(
                                        state = state,
                                        onTransform = { zoom, pan -> viewModel.updateTransform(zoom, pan) },
                                        onEventDrag = { id, drag -> viewModel.updateEventPosition(id, drag) },
                                        onEventDragEnd = { id -> viewModel.persistEventPosition(projectId, id) },
                                        onEventTap = { event ->
                                            if (state.connectionSourceId != null) {
                                                if (state.connectionSourceId != event.id) {
                                                    pendingTarget = event
                                                    showConnectionDialog = true
                                                }
                                            } else {
                                                viewModel.selectEvent(event)
                                            }
                                        },
                                        onEmptyTap = {
                                            if (!isAiReadOnly && state.connectionSourceId == null) {
                                                editingEvent = null
                                                showEventDialog = true
                                            }
                                        },
                                        onFitToScreen = { scale, offset, minScale, maxScale ->
                                            viewModel.initScale(scale, offset, minScale, maxScale)
                                        },
                                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                                    )
                            }

                        EditorTab.CHARACTERS ->
                            CharactersTab(
                                characters = state.characters,
                                onEdit = { character ->
                                    editingCharacter = character
                                    showCharacterDialog = true
                                },
                                onDelete = { id -> viewModel.deleteCharacter(id) },
                            )

                        EditorTab.MANUSCRIPT ->
                            ManuscriptTab(
                                title = state.manuscriptTitle,
                                description = state.manuscriptDescription,
                                chapters = state.chapters,
                                openChapter = state.chapters.find { it.id == state.openChapterId },
                                chaptered = isAiReadOnly,
                                chapterLoading = state.isChapterLoading,
                                chapterSaving = state.isChapterSaving,
                                chapterSaved = state.chapterSaved,
                                chapterError = state.chapterError,
                                onTitleChange = { viewModel.updateManuscriptTitle(it) },
                                onDescriptionChange = { viewModel.updateManuscriptDescription(it) },
                                onOpenChapter = { viewModel.openChapter(projectId, it) },
                                onCloseChapter = { viewModel.openChapter(projectId, null) },
                                onAddChapter = { viewModel.addChapter() },
                                onSaveChapter = { id -> viewModel.saveChapter(projectId, id) },
                                onBeginEditChapter = { id -> viewModel.beginEditChapter(id) },
                                onChapterTextChange = { id, newText, newBold, newItalic ->
                                    viewModel.updateChapterText(id, newText, newBold, newItalic)
                                },
                                onChapterAlignChange = { id, newAlign -> viewModel.setChapterAlign(id, newAlign) },
                            )
                    }
                }

                if (state.connectionSourceId != null) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(8.dp)
                                .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.connection_pick_target),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                        IconButton(onClick = { viewModel.cancelConnectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
    }

    state.selectedEvent?.let { selected ->
        EventActionDialog(
            event = selected,
            connectionLabels = state,
            isAiReadOnly = isAiReadOnly,
            onDismiss = { viewModel.selectEvent(null) },
            onEdit = {
                editingEvent = selected
                showEventDialog = true
                viewModel.selectEvent(null)
            },
            onStartLink = {
                viewModel.startConnectionMode(selected.id)
                viewModel.selectEvent(null)
            },
            onDelete = { viewModel.deleteEvent(selected.id) },
            onDeleteConnection = { id -> viewModel.deleteConnection(id) },
            onChangeColor = { color -> viewModel.updateEventColor(selected.id, color) },
        )
    }

    if (showEventDialog) {
        AddEventDialog(
            initial = editingEvent,
            availableCharacters = state.characters,
            availableTags = state.tags,
            onCreateCharacter = { name, desc, significance ->
                viewModel.addCharacter(name, desc, significance)
            },
            onCreateTag = { label, colorArgb -> viewModel.addTag(label, colorArgb) },
            onDismiss = {
                showEventDialog = false
                editingEvent = null
            },
            onConfirm = { title, shortDescription, description, impact, eventDate, characterIds, tagIds ->
                val editing = editingEvent
                if (editing == null) {
                    viewModel.addEvent(title, shortDescription, description, impact, eventDate, characterIds, tagIds, null)
                    viewModel.autoLayout(density)
                } else {
                    viewModel.updateEvent(editing.id, title, shortDescription, description, impact, eventDate, characterIds, tagIds)
                    viewModel.autoLayout(density)
                }
                showEventDialog = false
                editingEvent = null
            },
        )
    }

    if (showCharacterDialog) {
        CharacterDialog(
            initial = editingCharacter,
            onDismiss = {
                showCharacterDialog = false
                editingCharacter = null
            },
            onConfirm = { name, description, significance ->
                val editing = editingCharacter
                if (editing == null) {
                    viewModel.addCharacter(name, description, significance)
                } else {
                    viewModel.updateCharacter(editing.id, name, description, significance)
                }
                showCharacterDialog = false
                editingCharacter = null
            },
        )
    }

    if (showConnectionDialog) {
        ConnectionDialog(
            onDismiss = {
                showConnectionDialog = false
                pendingTarget = null
            },
            onConfirm = { type, description ->
                pendingTarget?.let { target ->
                    viewModel.completeConnection(target.id, type, description)
                }
                showConnectionDialog = false
                pendingTarget = null
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventActionDialog(
    event: EditorEvent,
    connectionLabels: EditorUiState,
    isAiReadOnly: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onStartLink: () -> Unit,
    onDelete: () -> Unit,
    onDeleteConnection: (String) -> Unit,
    onChangeColor: (Long?) -> Unit,
) {
    val relatedConnections =
        connectionLabels.connections.filter { it.sourceId == event.id || it.targetId == event.id }
    val eventCharacters = event.characterIds.mapNotNull { id -> connectionLabels.characters.find { it.id == id } }
    val eventTags = event.tagIds.mapNotNull { id -> connectionLabels.tags.find { it.id == id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                if (event.shortDescription.isNotBlank()) {
                    Text(event.shortDescription, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (event.description.isNotBlank()) {
                    Text(event.description, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (eventTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        eventTags.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = { Text(tag.label, style = MaterialTheme.typography.labelSmall) },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(tag.colorArgb).copy(alpha = 0.25f),
                                    ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (eventCharacters.isNotEmpty()) {
                    Text(
                        text = eventCharacters.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (relatedConnections.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.connection_list_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(relatedConnections) { connection ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stringResource(connectionTypeLabel(connection.type)))
                                if (!isAiReadOnly) {
                                    IconButton(onClick = { onDeleteConnection(connection.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                                    }
                                }
                            }
                        }
                    }
                }
                if (isAiReadOnly) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.event_action_change_color),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EventColorPalette(
                        selectedColor = event.colorArgb,
                        onSelect = onChangeColor,
                    )
                }
            }
        },
        confirmButton = {
            if (isAiReadOnly) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_close))
                }
            } else {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.event_action_edit))
                }
            }
        },
        dismissButton = {
            if (!isAiReadOnly) {
                Row {
                    IconButton(onClick = onStartLink) {
                        Icon(Icons.Default.Link, contentDescription = stringResource(R.string.event_action_link))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.event_action_delete))
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventColorPalette(
    selectedColor: Long?,
    onSelect: (Long?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EVENT_TAG_PALETTE.forEach { colorArgb ->
            val isSelected = selectedColor == colorArgb
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(colorArgb))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        )
                        .clickable { onSelect(colorArgb) },
            )
        }
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape)
                    .clickable { onSelect(null) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.dialog_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rememberDefaultEventTags(): List<EventTag> =
    DEFAULT_TAG_SPECS.map { spec ->
        EventTag(
            id = spec.id,
            label = stringResource(tagRoleLabel(spec.roleKey)),
            colorArgb = spec.colorArgb,
            roleKey = spec.roleKey,
        )
    }

private fun tagRoleLabel(roleKey: String): Int =
    when (roleKey) {
        "INCITING_INCIDENT" -> R.string.tag_inciting
        "RISING_ACTION" -> R.string.tag_rising
        "CLIMAX" -> R.string.tag_climax
        "FALLING_ACTION" -> R.string.tag_falling
        "RESOLUTION" -> R.string.tag_resolution
        "PLOT_TWIST" -> R.string.tag_twist
        else -> R.string.tag_regular
    }
