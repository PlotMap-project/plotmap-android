package com.plotmap.app.core.models

import androidx.compose.ui.geometry.Offset
import java.util.UUID

const val EVENT_SHORT_DESCRIPTION_MAX = 60
const val EVENT_IMPACT_MIN = 1
const val EVENT_IMPACT_MAX = 10

enum class EditorTab { GRAPH, CHARACTERS }

enum class EditorMode { MANUAL, AI_READONLY }

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
        DefaultTagSpec("tag_inciting", "INCITING_INCIDENT", 0xFFA83F39L),
        DefaultTagSpec("tag_rising", "RISING_ACTION", 0xFFC4853AL),
        DefaultTagSpec("tag_climax", "CLIMAX", 0xFF8E3B6BL),
        DefaultTagSpec("tag_falling", "FALLING_ACTION", 0xFF4A6FA5L),
        DefaultTagSpec("tag_resolution", "RESOLUTION", 0xFF5A8A55L),
        DefaultTagSpec("tag_twist", "PLOT_TWIST", 0xFF7A4FA3L),
        DefaultTagSpec("tag_regular", "REGULAR", 0xFF947158L),
    )

val EVENT_TAG_PALETTE =
    listOf(
        0xFFA83F39L,
        0xFFC4853AL,
        0xFF8E3B6BL,
        0xFF4A6FA5L,
        0xFF5A8A55L,
        0xFF7A4FA3L,
        0xFF947158L,
        0xFF3D2917L,
    )

fun EditorConnectionType.toApiValue(): String =
    when (this) {
        EditorConnectionType.CAUSAL -> "CAUSAL"
        EditorConnectionType.TEMPORAL -> "TEMPORAL"
        EditorConnectionType.PARALLEL -> "PARALLEL"
    }
