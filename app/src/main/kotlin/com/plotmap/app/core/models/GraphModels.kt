package com.plotmap.app.core.models

import androidx.compose.ui.geometry.Offset
import java.util.UUID

const val EVENT_SHORT_DESCRIPTION_MAX = 60
const val EVENT_IMPACT_MIN = 1
const val EVENT_IMPACT_MAX = 10

enum class EditorTab { GRAPH, CHARACTERS, MANUSCRIPT }

enum class ManuscriptAlign { START, CENTER, END, JUSTIFY }

enum class EditorMode { MANUAL, AI_READONLY }

data class ManuscriptChapter(
    val id: String = UUID.randomUUID().toString(),
    val order: Int = 1,
    val title: String? = null,
    val text: String = "",
    val bold: List<Boolean> = emptyList(),
    val italic: List<Boolean> = emptyList(),
    val align: ManuscriptAlign = ManuscriptAlign.START,
    val locked: Boolean = false,
    val loaded: Boolean = false,
    val serverBacked: Boolean = false,
)

enum class CharacterSignificance { MAIN, SECONDARY, EPISODIC }

enum class EditorConnectionType { CAUSAL, TEMPORAL, PARALLEL }

data class EditorEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val shortDescription: String = "",
    val description: String = "",
    val impactLevel: Int = 5,
    val eventDate: String = "",
    val characterIds: List<String> = emptyList(),
    val tagIds: List<String> = emptyList(),
    val position: Offset? = null,
    val colorArgb: Long? = null,
    val isManuallyPositioned: Boolean = false,
    val level: Int? = null,
    val orderInLevel: Int? = null,
)

data class EditorGraph(
    val projectId: String,
    val title: String,
    val events: List<EditorEvent>,
    val connections: List<EditorConnection>,
    val characters: List<EditorCharacter> = emptyList(),
    val description: String = "",
    val sourceText: String = "",
)

data class EditorConnection(
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val targetId: String,
    val type: EditorConnectionType = EditorConnectionType.CAUSAL,
    val description: String = "",
    val colorArgb: Long? = null,
)

data class EditorCharacter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val significance: CharacterSignificance = CharacterSignificance.SECONDARY,
    val colorArgb: Long? = null,
)

data class EventTag(
    val id: String,
    val label: String,
    val colorArgb: Long,
    val isCustom: Boolean = false,
    val roleKey: String? = null,
)

data class DefaultTagSpec(
    val id: String,
    val roleKey: String,
    val colorArgb: Long,
)

val DEFAULT_TAG_SPECS =
    listOf(
        DefaultTagSpec("tag_inciting", "INCITING_INCIDENT", 0xFFCC2244L),
        DefaultTagSpec("tag_rising", "RISING_ACTION", 0xFFB8860BL),
        DefaultTagSpec("tag_climax", "CLIMAX", 0xFF9B1DCAL),
        DefaultTagSpec("tag_falling", "FALLING_ACTION", 0xFF2E5FAAL),
        DefaultTagSpec("tag_resolution", "RESOLUTION", 0xFF1E7B45L),
        DefaultTagSpec("tag_twist", "PLOT_TWIST", 0xFFD4278CL),
        DefaultTagSpec("tag_regular", "REGULAR", 0xFF6B5A8EL),
    )

val EVENT_TAG_PALETTE =
    listOf(
        0xFFCC2244L,
        0xFFB8860BL,
        0xFF9B1DCAL,
        0xFF2E5FAAL,
        0xFF1E7B45L,
        0xFFD4278CL,
        0xFF6B5A8EL,
        0xFF3E2B7AL,
    )

fun EditorConnectionType.toApiValue(): String =
    when (this) {
        EditorConnectionType.CAUSAL -> "CAUSAL"
        EditorConnectionType.TEMPORAL -> "TEMPORAL"
        EditorConnectionType.PARALLEL -> "PARALLEL"
    }

fun editorConnectionTypeFromApi(value: String): EditorConnectionType =
    when (value.uppercase()) {
        "TEMPORAL" -> EditorConnectionType.TEMPORAL
        "PARALLEL" -> EditorConnectionType.PARALLEL
        else -> EditorConnectionType.CAUSAL
    }

fun characterSignificanceFromApi(value: String?): CharacterSignificance =
    when (value?.uppercase()) {
        "PROTAGONIST", "MAJOR" -> CharacterSignificance.MAIN
        "EPISODIC" -> CharacterSignificance.EPISODIC
        else -> CharacterSignificance.SECONDARY
    }
