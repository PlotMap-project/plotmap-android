package com.plotmap.app.feature.editor.workspace.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.plotmap.app.core.models.EVENT_IMPACT_MAX
import com.plotmap.app.core.models.EVENT_IMPACT_MIN
import com.plotmap.app.core.models.EditorEvent
import com.plotmap.app.feature.editor.workspace.EditorUiState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val NodeMinSize = 96.dp
private val NodeMaxSize = 172.dp

@Composable
fun GraphCanvas(
    state: EditorUiState,
    onTransform: (zoom: Float, pan: Offset) -> Unit,
    onEventDrag: (id: String, dragAmount: Offset) -> Unit,
    onEventDragEnd: (id: String) -> Unit,
    onEventTap: (EditorEvent) -> Unit,
    onEmptyTap: () -> Unit,
    onFitToScreen: (scale: Float, offset: Offset, minScale: Float, maxScale: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val connectionColor = MaterialTheme.colorScheme.onSurfaceVariant
    var fittedToScreen by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()
        val nodeMinPx = with(density) { NodeMinSize.toPx() }
        val nodeMaxPx = with(density) { NodeMaxSize.toPx() }

        LaunchedEffect(state.events.size) {
            if (state.events.isNotEmpty() && !fittedToScreen) {
                var minX = Float.MAX_VALUE
                var minY = Float.MAX_VALUE
                var maxX = -Float.MAX_VALUE
                var maxY = -Float.MAX_VALUE

                state.events.forEach { event ->
                    val pos = event.position ?: Offset.Zero
                    val size = with(density) { nodeSize(event.impactLevel).toPx() }
                    if (pos.x < minX) minX = pos.x
                    if (pos.y < minY) minY = pos.y
                    if (pos.x + size > maxX) maxX = pos.x + size
                    if (pos.y + size > maxY) maxY = pos.y + size
                }

                val graphW = (maxX - minX).coerceAtLeast(nodeMaxPx)
                val graphH = (maxY - minY).coerceAtLeast(nodeMaxPx)
                val graphCenterX = (minX + maxX) / 2f
                val graphCenterY = (minY + maxY) / 2f

                val fitScaleX = screenW / (graphW * 3f)
                val fitScaleY = screenH / (graphH * 3f)
                val fitScale = minOf(fitScaleX, fitScaleY).coerceAtLeast(0.05f)
                val maxScale = (minOf(screenW, screenH) / nodeMinPx).coerceAtLeast(fitScale)

                val screenCenterX = screenW / 2f
                val screenCenterY = screenH / 2f
                val fitOffset =
                    Offset(
                        x = (screenCenterX - graphCenterX) * fitScale,
                        y = (screenCenterY - graphCenterY) * fitScale,
                    )

                onFitToScreen(fitScale, fitOffset, fitScale, maxScale)
                fittedToScreen = true
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onTransform(zoom, pan)
                        }
                    },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = state.scale
                            scaleY = state.scale
                            translationX = state.offset.x
                            translationY = state.offset.y
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onEmptyTap() })
                        },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.connections.forEach { connection ->
                        val source = state.events.find { it.id == connection.sourceId }
                        val target = state.events.find { it.id == connection.targetId }
                        if (source != null && target != null) {
                            val sourceRadius = with(density) { nodeSize(source.impactLevel).toPx() } / 2f
                            val targetRadius = with(density) { nodeSize(target.impactLevel).toPx() } / 2f
                            val sourceCenter = (source.position ?: Offset.Zero) + Offset(sourceRadius, sourceRadius)
                            val targetCenter = (target.position ?: Offset.Zero) + Offset(targetRadius, targetRadius)
                            drawArrow(
                                from = sourceCenter,
                                to = targetCenter,
                                fromRadius = sourceRadius,
                                toRadius = targetRadius,
                                color = connection.colorArgb?.let { Color(it) } ?: connectionColor,
                            )
                        }
                    }
                }

                state.events.forEach { event ->
                    EventNode(
                        event = event,
                        accent = nodeAccent(event, state),
                        selected = state.selectedEvent?.id == event.id,
                        onDrag = { dragAmount -> onEventDrag(event.id, dragAmount / state.scale) },
                        onDragEnd = { onEventDragEnd(event.id) },
                        onTap = { onEventTap(event) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EventNode(
    event: EditorEvent,
    accent: Color?,
    selected: Boolean,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit,
) {
    val position = event.position ?: Offset.Zero
    val size = nodeSize(event.impactLevel)
    val container =
        event.colorArgb?.let { Color(it) }
            ?: if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    val textColor = contrastTextColor(container)
    val borderColor =
        when {
            accent != null -> accent
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        }

    Box(
        modifier =
            Modifier
                .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(container)
                .border(
                    width = if (selected) 3.dp else 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp),
                )
                .pointerInput(event.id) {
                    detectDragGestures(onDragEnd = { onDragEnd() }) { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                }
                .pointerInput(event.id) {
                    detectTapGestures(onTap = { onTap() })
                }
                .padding(12.dp),
    ) {
        Column {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.shortDescription.isNotBlank()) {
                Text(
                    text = event.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun contrastTextColor(bgColor: Color): Color {
    val luminance = 0.299f * bgColor.red + 0.587f * bgColor.green + 0.114f * bgColor.blue
    return if (luminance > 0.45f) Color(0xFF1A1100) else Color(0xFFF5ECD7)
}

private fun nodeSize(impactLevel: Int): Dp {
    val clamped = impactLevel.coerceIn(EVENT_IMPACT_MIN, EVENT_IMPACT_MAX)
    val fraction = (clamped - EVENT_IMPACT_MIN).toFloat() / (EVENT_IMPACT_MAX - EVENT_IMPACT_MIN)
    return lerp(NodeMinSize, NodeMaxSize, fraction)
}

private fun nodeAccent(
    event: EditorEvent,
    state: EditorUiState,
): Color? {
    val tag = event.tagIds.firstNotNullOfOrNull { id -> state.tags.find { it.id == id } }
    return tag?.let { Color(it.colorArgb) }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    from: Offset,
    to: Offset,
    fromRadius: Float,
    toRadius: Float,
    color: Color,
) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val distance = sqrt(dx * dx + dy * dy)
    if (distance <= fromRadius + toRadius) return
    val dirX = dx / distance
    val dirY = dy / distance
    val start = Offset(from.x + dirX * fromRadius, from.y + dirY * fromRadius)
    val end = Offset(to.x - dirX * toRadius, to.y - dirY * toRadius)
    val strokeWidth = 5f
    drawLine(color = color, start = start, end = end, strokeWidth = strokeWidth)

    val angle = atan2(end.y - start.y, end.x - start.x)
    val headLength = 28f
    val headSpread = 0.45f
    val left =
        Offset(
            end.x - headLength * cos(angle - headSpread),
            end.y - headLength * sin(angle - headSpread),
        )
    val right =
        Offset(
            end.x - headLength * cos(angle + headSpread),
            end.y - headLength * sin(angle + headSpread),
        )
    drawLine(color = color, start = end, end = left, strokeWidth = strokeWidth)
    drawLine(color = color, start = end, end = right, strokeWidth = strokeWidth)
}
