package com.plotmap.app.feature.editor.workspace.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.models.ManuscriptAlign
import com.plotmap.app.core.models.ManuscriptChapter

private const val MANUSCRIPT_INDENT = "    "
private const val SCENE_BREAK = "\n* * *\n"
private const val HEADER_SCROLL_THRESHOLD = 2f
private const val CHAPTER_PREVIEW_MAX = 120

@Composable
fun ManuscriptTab(
    title: String,
    description: String,
    chapters: List<ManuscriptChapter>,
    openChapter: ManuscriptChapter?,
    chaptered: Boolean,
    chapterLoading: Boolean,
    chapterSaving: Boolean,
    chapterSaved: Boolean,
    chapterError: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onOpenChapter: (String) -> Unit,
    onCloseChapter: () -> Unit,
    onAddChapter: () -> Unit,
    onSaveChapter: (id: String) -> Unit,
    onBeginEditChapter: (id: String) -> Unit,
    onChapterTextChange: (id: String, text: String, bold: List<Boolean>, italic: List<Boolean>) -> Unit,
    onChapterAlignChange: (id: String, align: ManuscriptAlign) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingEditId by remember { mutableStateOf<String?>(null) }

    if (!chaptered) {
        val chapter = chapters.firstOrNull() ?: ManuscriptChapter()
        ChapterEditor(
            title = title,
            description = description,
            chapter = chapter,
            chapterLabel = null,
            loading = false,
            saving = false,
            saved = false,
            error = null,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onBack = null,
            onSave = null,
            onRequestEdit = null,
            onTextChange = onChapterTextChange,
            onAlignChange = onChapterAlignChange,
            modifier = modifier,
        )
        return
    }

    if (openChapter == null) {
        ChapterListView(
            title = title,
            description = description,
            chapters = chapters,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onOpenChapter = onOpenChapter,
            onAddChapter = onAddChapter,
            modifier = modifier,
        )
    } else {
        val label = stringResource(R.string.manuscript_chapter_number, openChapter.order)
        val readOnly = openChapter.locked
        ChapterEditor(
            title = title,
            description = description,
            chapter = openChapter,
            chapterLabel = label,
            loading = chapterLoading,
            saving = chapterSaving,
            saved = chapterSaved,
            error = chapterError,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onBack = onCloseChapter,
            onSave = if (readOnly) null else { -> onSaveChapter(openChapter.id) },
            onRequestEdit = if (readOnly && openChapter.serverBacked) { -> pendingEditId = openChapter.id } else null,
            onTextChange = onChapterTextChange,
            onAlignChange = onChapterAlignChange,
            modifier = modifier,
        )
    }

    pendingEditId?.let { editId ->
        AlertDialog(
            onDismissRequest = { pendingEditId = null },
            title = { Text(stringResource(R.string.manuscript_edit_warning_title)) },
            text = { Text(stringResource(R.string.manuscript_edit_warning_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onBeginEditChapter(editId)
                    pendingEditId = null
                }) {
                    Text(stringResource(R.string.manuscript_edit_warning_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingEditId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ChapterListView(
    title: String,
    description: String,
    chapters: List<ManuscriptChapter>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onOpenChapter: (String) -> Unit,
    onAddChapter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().imePadding()) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.manuscript_title_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.manuscript_description_label)) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(chapters) { _, chapter ->
                ChapterCard(
                    label = stringResource(R.string.manuscript_chapter_number, chapter.order),
                    chapter = chapter,
                    onClick = { onOpenChapter(chapter.id) },
                )
            }
            item {
                AddChapterCard(onClick = onAddChapter)
            }
        }
    }
}

@Composable
private fun ChapterCard(
    label: String,
    chapter: ManuscriptChapter,
    onClick: () -> Unit,
) {
    val preview = chapter.text.trim().take(CHAPTER_PREVIEW_MAX)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (chapter.locked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.manuscript_chapter_readonly),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (preview.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AddChapterCard(onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.manuscript_add_chapter),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.manuscript_add_chapter),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ChapterEditor(
    title: String,
    description: String,
    chapter: ManuscriptChapter,
    chapterLabel: String?,
    loading: Boolean,
    saving: Boolean,
    saved: Boolean,
    error: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBack: (() -> Unit)?,
    onSave: (() -> Unit)?,
    onRequestEdit: (() -> Unit)?,
    onTextChange: (id: String, text: String, bold: List<Boolean>, italic: List<Boolean>) -> Unit,
    onAlignChange: (id: String, align: ManuscriptAlign) -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(chapter.id) { mutableStateOf(TextFieldValue(chapter.text, TextRange(chapter.text.length))) }

    LaunchedEffect(chapter.id, chapter.text) {
        if (chapter.text != value.text) {
            value = value.copy(text = chapter.text, selection = TextRange(chapter.text.length))
        }
    }

    fun applyEdit(newValue: TextFieldValue) {
        val newBold = remapFlags(chapter.bold, value.text, newValue.text)
        val newItalic = remapFlags(chapter.italic, value.text, newValue.text)
        value = newValue
        onTextChange(chapter.id, newValue.text, newBold, newItalic)
    }

    val textAlign =
        when (chapter.align) {
            ManuscriptAlign.START -> TextAlign.Start
            ManuscriptAlign.CENTER -> TextAlign.Center
            ManuscriptAlign.END -> TextAlign.End
            ManuscriptAlign.JUSTIFY -> TextAlign.Justify
        }
    val bodyStyle =
        MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = textAlign,
        )

    var headerVisible by remember { mutableStateOf(true) }
    val headerScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y < -HEADER_SCROLL_THRESHOLD) {
                        headerVisible = false
                    } else if (available.y > HEADER_SCROLL_THRESHOLD) {
                        headerVisible = true
                    }
                    return Offset.Zero
                }
            }
        }

    Column(modifier = modifier.fillMaxSize().imePadding().nestedScroll(headerScrollConnection)) {
        AnimatedVisibility(visible = headerVisible) {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.manuscript_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.manuscript_description_label)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        if (chapterLabel != null && onBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.manuscript_back_to_chapters),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = chapterLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (chapter.locked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.manuscript_chapter_readonly),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp).padding(end = 4.dp),
                    )
                }
                if (onRequestEdit != null) {
                    IconButton(onClick = onRequestEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (onSave != null) {
                    TextButton(
                        onClick = onSave,
                        enabled = !saving && !saved && value.text.isNotBlank(),
                    ) {
                        Text(
                            text =
                                stringResource(
                                    if (saved) R.string.manuscript_chapter_saved else R.string.manuscript_save_chapter,
                                ),
                        )
                    }
                }
            }
        }

        when {
            error != null ->
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.manuscript_chapter_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

            loading ->
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            else -> {
                if (!chapter.locked) {
                    ManuscriptToolbar(
                        align = chapter.align,
                        onAlign = { onAlignChange(chapter.id, it) },
                        onBold = {
                            onTextChange(
                                chapter.id,
                                value.text,
                                toggleRange(chapter.bold, value.selection.min, value.selection.max),
                                chapter.italic,
                            )
                        },
                        onItalic = {
                            onTextChange(
                                chapter.id,
                                value.text,
                                chapter.bold,
                                toggleRange(chapter.italic, value.selection.min, value.selection.max),
                            )
                        },
                        onIndent = { applyEdit(indentEdit(value)) },
                        onSceneBreak = { applyEdit(sceneBreakEdit(value)) },
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = { if (!chapter.locked) applyEdit(it) },
                    readOnly = chapter.locked,
                    textStyle = bodyStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    visualTransformation = manuscriptVisualTransformation(chapter.bold, chapter.italic),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 8.dp),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (value.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.manuscript_text_placeholder),
                                    style = bodyStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)),
                                )
                            }
                            inner()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ManuscriptToolbar(
    align: ManuscriptAlign,
    onAlign: (ManuscriptAlign) -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onIndent: () -> Unit,
    onSceneBreak: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarIcon(Icons.AutoMirrored.Filled.FormatAlignLeft, R.string.manuscript_align_left, align == ManuscriptAlign.START) {
            onAlign(ManuscriptAlign.START)
        }
        ToolbarIcon(Icons.Filled.FormatAlignCenter, R.string.manuscript_align_center, align == ManuscriptAlign.CENTER) {
            onAlign(ManuscriptAlign.CENTER)
        }
        ToolbarIcon(Icons.AutoMirrored.Filled.FormatAlignRight, R.string.manuscript_align_right, align == ManuscriptAlign.END) {
            onAlign(ManuscriptAlign.END)
        }
        ToolbarIcon(Icons.Filled.FormatAlignJustify, R.string.manuscript_align_justify, align == ManuscriptAlign.JUSTIFY) {
            onAlign(ManuscriptAlign.JUSTIFY)
        }
        ToolbarSeparator()
        ToolbarIcon(Icons.AutoMirrored.Filled.FormatIndentIncrease, R.string.manuscript_indent, false) { onIndent() }
        ToolbarIcon(Icons.Filled.FormatBold, R.string.manuscript_bold, false) { onBold() }
        ToolbarIcon(Icons.Filled.FormatItalic, R.string.manuscript_italic, false) { onItalic() }
        IconButton(onClick = onSceneBreak) {
            Text(
                text = "✳✳✳",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToolbarIcon(
    icon: ImageVector,
    descriptionRes: Int,
    active: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(descriptionRes),
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ToolbarSeparator() {
    Box(
        modifier =
            Modifier
                .padding(horizontal = 6.dp)
                .width(1.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(1.dp)),
    )
}

private fun manuscriptVisualTransformation(
    bold: List<Boolean>,
    italic: List<Boolean>,
): VisualTransformation =
    object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val raw = text.text
            val styled =
                buildAnnotatedString {
                    append(raw)
                    var i = 0
                    val n = raw.length
                    while (i < n) {
                        val b = bold.getOrElse(i) { false }
                        val it = italic.getOrElse(i) { false }
                        if (!b && !it) {
                            i++
                            continue
                        }
                        var j = i + 1
                        while (j < n && bold.getOrElse(j) { false } == b && italic.getOrElse(j) { false } == it) j++
                        addStyle(
                            SpanStyle(
                                fontWeight = if (b) FontWeight.Bold else null,
                                fontStyle = if (it) FontStyle.Italic else null,
                            ),
                            i,
                            j,
                        )
                        i = j
                    }
                }
            return TransformedText(styled, OffsetMapping.Identity)
        }
    }

private fun ensureLength(
    flags: List<Boolean>,
    length: Int,
): List<Boolean> =
    when {
        flags.size == length -> flags
        flags.size > length -> flags.subList(0, length)
        else -> flags + List(length - flags.size) { false }
    }

private fun remapFlags(
    old: List<Boolean>,
    oldText: String,
    newText: String,
): List<Boolean> {
    if (oldText == newText) return ensureLength(old, newText.length)
    val oldLen = oldText.length
    val newLen = newText.length
    var prefix = 0
    while (prefix < oldLen && prefix < newLen && oldText[prefix] == newText[prefix]) prefix++
    var suffix = 0
    while (
        suffix < oldLen - prefix &&
        suffix < newLen - prefix &&
        oldText[oldLen - 1 - suffix] == newText[newLen - 1 - suffix]
    ) {
        suffix++
    }
    val insertedLen = newLen - prefix - suffix
    val safeOld = ensureLength(old, oldLen)
    val inherit = if (prefix > 0) safeOld[prefix - 1] else false
    val result = ArrayList<Boolean>(newLen)
    result.addAll(safeOld.subList(0, prefix))
    repeat(insertedLen) { result.add(inherit) }
    result.addAll(safeOld.subList(oldLen - suffix, oldLen))
    return result
}

private fun toggleRange(
    flags: List<Boolean>,
    start: Int,
    end: Int,
): List<Boolean> {
    if (start >= end) return flags
    val safe = ensureLength(flags, maxOf(flags.size, end))
    val allSet = (start until end).all { safe[it] }
    return safe.mapIndexed { index, current -> if (index in start until end) !allSet else current }
}

private fun indentEdit(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val cursor = value.selection.min
    val lineStart = if (cursor == 0) 0 else text.lastIndexOf('\n', cursor - 1) + 1
    val newText = text.substring(0, lineStart) + MANUSCRIPT_INDENT + text.substring(lineStart)
    val shift = MANUSCRIPT_INDENT.length
    return TextFieldValue(
        text = newText,
        selection = TextRange(value.selection.min + shift, value.selection.max + shift),
    )
}

private fun sceneBreakEdit(value: TextFieldValue): TextFieldValue {
    val text = value.text
    val at = value.selection.max
    val newText = text.substring(0, at) + SCENE_BREAK + text.substring(at)
    return TextFieldValue(
        text = newText,
        selection = TextRange(at + SCENE_BREAK.length),
    )
}
