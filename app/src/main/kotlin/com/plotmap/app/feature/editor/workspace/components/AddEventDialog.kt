package com.plotmap.app.feature.editor.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.models.CharacterSignificance
import com.plotmap.app.core.models.EVENT_IMPACT_MAX
import com.plotmap.app.core.models.EVENT_IMPACT_MIN
import com.plotmap.app.core.models.EVENT_SHORT_DESCRIPTION_MAX
import com.plotmap.app.core.models.EditorCharacter
import com.plotmap.app.core.models.EditorEvent
import com.plotmap.app.core.models.EventTag
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    initial: EditorEvent?,
    availableCharacters: List<EditorCharacter>,
    availableTags: List<EventTag>,
    onCreateCharacter: (name: String, description: String, significance: CharacterSignificance) -> String,
    onCreateTag: (label: String, colorArgb: Long) -> String,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        shortDescription: String,
        description: String,
        impactLevel: Int,
        eventDate: String,
        characterIds: List<String>,
        tagIds: List<String>,
    ) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var shortDescription by remember { mutableStateOf(initial?.shortDescription.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var impactLevel by remember { mutableStateOf((initial?.impactLevel ?: 5).toFloat()) }
    var eventDate by remember { mutableStateOf(initial?.eventDate.orEmpty()) }
    val selectedCharacterIds = remember { mutableStateListOf<String>().apply { initial?.characterIds?.let { addAll(it) } } }
    val selectedTagIds = remember { mutableStateListOf<String>().apply { initial?.tagIds?.let { addAll(it) } } }

    var characterMenuExpanded by remember { mutableStateOf(false) }
    var showNewCharacter by remember { mutableStateOf(false) }
    var showNewTag by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = dateStringToMillis(eventDate),
        )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(0.94f)
                    .heightIn(max = 720.dp)
                    .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text =
                        stringResource(
                            if (initial == null) R.string.event_create_title else R.string.event_edit_title,
                        ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier =
                        Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.event_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shortDescription,
                        onValueChange = {
                            if (it.length <= EVENT_SHORT_DESCRIPTION_MAX) shortDescription = it
                        },
                        label = { Text(stringResource(R.string.event_short_description)) },
                        supportingText = {
                            Text(
                                stringResource(
                                    R.string.event_short_desc_counter,
                                    shortDescription.length,
                                    EVENT_SHORT_DESCRIPTION_MAX,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.event_description_full)) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        maxLines = 8,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.event_impact_level, impactLevel.toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = impactLevel,
                        onValueChange = { impactLevel = it },
                        valueRange = EVENT_IMPACT_MIN.toFloat()..EVENT_IMPACT_MAX.toFloat(),
                        steps = EVENT_IMPACT_MAX - EVENT_IMPACT_MIN - 1,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.event_date)) },
                        placeholder = { Text(stringResource(R.string.event_date_placeholder)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.event_characters),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        selectedCharacterIds.forEach { id ->
                            val character = availableCharacters.find { it.id == id }
                            if (character != null) {
                                InputChip(
                                    selected = true,
                                    onClick = { selectedCharacterIds.remove(id) },
                                    label = { Text(character.name) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { characterMenuExpanded = true }
                                    .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.event_add_character),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        DropdownMenu(
                            expanded = characterMenuExpanded,
                            onDismissRequest = { characterMenuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.event_new_character)) },
                                onClick = {
                                    characterMenuExpanded = false
                                    showNewCharacter = true
                                },
                            )
                            availableCharacters
                                .filter { it.id !in selectedCharacterIds }
                                .forEach { character ->
                                    DropdownMenuItem(
                                        text = { Text(character.name) },
                                        onClick = {
                                            selectedCharacterIds.add(character.id)
                                            characterMenuExpanded = false
                                        },
                                    )
                                }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.event_tags),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        availableTags.forEach { tag ->
                            val isSelected = tag.id in selectedTagIds
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTagIds.remove(tag.id) else selectedTagIds.add(tag.id)
                                },
                                label = { Text(tag.label, overflow = TextOverflow.Ellipsis, maxLines = 1) },
                                leadingIcon = {
                                    Spacer(
                                        modifier =
                                            Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(tag.colorArgb)),
                                    )
                                },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(tag.colorArgb).copy(alpha = 0.25f),
                                    ),
                            )
                        }
                        InputChip(
                            selected = false,
                            onClick = { showNewTag = true },
                            label = { Text(stringResource(R.string.event_new_tag)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                title.trim(),
                                shortDescription.trim(),
                                description.trim(),
                                impactLevel.toInt(),
                                eventDate,
                                selectedCharacterIds.toList(),
                                selectedTagIds.toList(),
                            )
                        },
                        enabled = title.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.dialog_done))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            eventDate = millisToDateString(millis)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showNewCharacter) {
        CharacterDialog(
            onDismiss = { showNewCharacter = false },
            onConfirm = { name, desc, significance ->
                val newId = onCreateCharacter(name, desc, significance)
                selectedCharacterIds.add(newId)
                showNewCharacter = false
            },
        )
    }

    if (showNewTag) {
        TagCreateDialog(
            onDismiss = { showNewTag = false },
            onConfirm = { label, colorArgb ->
                val newId = onCreateTag(label, colorArgb)
                selectedTagIds.add(newId)
                showNewTag = false
            },
        )
    }
}

private fun dateStringToMillis(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    val parts = dateStr.split(".")
    if (parts.size != 3) return null
    return try {
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val year = parts[2].toInt()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(year, month, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    } catch (_: Exception) {
        null
    }
}

private fun millisToDateString(millis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = cal.get(Calendar.YEAR).toString()
    return "$day.$month.$year"
}
