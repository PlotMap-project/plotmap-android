package com.plotmap.app.feature.settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.plotmap.app.R
import com.plotmap.app.core.data.PreferencesManager
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.core.designsystem.components.PlotMapBackButton
import org.koin.java.KoinJavaComponent.inject

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onFontSizeChange: (String) -> Unit = {},
    onListHeightChange: (String) -> Unit = {},
    onSortByChange: (String) -> Unit = {},
) {
    val preferencesManager: PreferencesManager by inject(PreferencesManager::class.java)
    val tokenManager: TokenManager by inject(TokenManager::class.java)
    var language by remember { mutableStateOf(preferencesManager.getLanguage()) }
    var fontSize by remember { mutableStateOf(preferencesManager.getFontSize()) }
    var listHeight by remember { mutableStateOf(preferencesManager.getListHeight()) }
    var sortBy by remember { mutableStateOf(preferencesManager.getSortBy()) }
    var autoBackup by remember { mutableStateOf(preferencesManager.isAutoBackupEnabled()) }
    var themeDialogExpanded by remember { mutableStateOf(false) }
    var tempTheme by remember { mutableStateOf(if (isDarkTheme) "dark" else "light") }
    var languageDialogExpanded by remember { mutableStateOf(false) }
    var fontSizeDialogExpanded by remember { mutableStateOf(false) }
    var listHeightDialogExpanded by remember { mutableStateOf(false) }
    var sortByDialogExpanded by remember { mutableStateOf(false) }
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlotMapBackButton(onClick = onBack, contentDescription = stringResource(R.string.btn_back))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = primaryTextColor,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            SettingClickableItem(
                label = stringResource(R.string.theme),
                value = if (isDarkTheme) stringResource(R.string.theme_dark) else stringResource(R.string.theme_light),
                textColor = primaryTextColor,
                onClick = {
                    tempTheme = if (isDarkTheme) "dark" else "light"
                    themeDialogExpanded = true
                },
            )
            if (themeDialogExpanded) {
                SelectionDialog(
                    title = stringResource(R.string.theme),
                    options =
                        listOf(
                            "light" to stringResource(R.string.theme_light),
                            "dark" to stringResource(R.string.theme_dark),
                        ),
                    selectedOption = tempTheme,
                    onOptionSelect = { tempTheme = it },
                    onConfirm = {
                        onThemeChange(tempTheme == "dark")
                        themeDialogExpanded = false
                    },
                    onDismiss = { themeDialogExpanded = false },
                )
            }
            SettingClickableItem(
                label = stringResource(R.string.language),
                value = if (language == "ru") stringResource(R.string.lang_ru) else "English",
                textColor = primaryTextColor,
                onClick = { languageDialogExpanded = true },
            )
            if (languageDialogExpanded) {
                SelectionDialog(
                    title = stringResource(R.string.language),
                    options = listOf("ru" to stringResource(R.string.lang_ru), "en" to "English"),
                    selectedOption = language,
                    onOptionSelect = { language = it },
                    onConfirm = {
                        preferencesManager.setLanguage(language)
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
                        languageDialogExpanded = false
                    },
                    onDismiss = { languageDialogExpanded = false },
                )
            }
            SettingClickableItem(
                label = stringResource(R.string.font_size),
                value =
                    when (fontSize) {
                        "small" -> stringResource(R.string.font_small)
                        "large" -> stringResource(R.string.font_large)
                        else -> stringResource(R.string.font_medium)
                    },
                textColor = primaryTextColor,
                onClick = { fontSizeDialogExpanded = true },
            )
            if (fontSizeDialogExpanded) {
                SelectionDialog(
                    title = stringResource(R.string.font_size),
                    options =
                        listOf(
                            "small" to stringResource(R.string.font_small),
                            "medium" to stringResource(R.string.font_medium),
                            "large" to stringResource(R.string.font_large),
                        ),
                    selectedOption = fontSize,
                    onOptionSelect = { fontSize = it },
                    onConfirm = {
                        preferencesManager.setFontSize(fontSize)
                        onFontSizeChange(fontSize)
                        fontSizeDialogExpanded = false
                    },
                    onDismiss = { fontSizeDialogExpanded = false },
                )
            }
            SettingClickableItem(
                label = stringResource(R.string.list_height),
                value =
                    when (listHeight) {
                        "small" -> stringResource(R.string.lh_small)
                        "large" -> stringResource(R.string.lh_large)
                        else -> stringResource(R.string.lh_medium)
                    },
                textColor = primaryTextColor,
                onClick = { listHeightDialogExpanded = true },
            )
            if (listHeightDialogExpanded) {
                SelectionDialog(
                    title = stringResource(R.string.list_height_short),
                    options =
                        listOf(
                            "small" to stringResource(R.string.lh_small),
                            "medium" to stringResource(R.string.lh_medium),
                            "large" to stringResource(R.string.lh_large),
                        ),
                    selectedOption = listHeight,
                    onOptionSelect = { listHeight = it },
                    onConfirm = {
                        preferencesManager.setListHeight(listHeight)
                        onListHeightChange(listHeight)
                        listHeightDialogExpanded = false
                    },
                    onDismiss = { listHeightDialogExpanded = false },
                )
            }
            SettingClickableItem(
                label = stringResource(R.string.sort_projects),
                value =
                    when (sortBy) {
                        "date_created" -> stringResource(R.string.sort_date_created)
                        "name" -> stringResource(R.string.sort_name)
                        else -> stringResource(R.string.sort_date_modified)
                    },
                textColor = primaryTextColor,
                onClick = { sortByDialogExpanded = true },
            )
            if (sortByDialogExpanded) {
                SelectionDialog(
                    title = stringResource(R.string.sort),
                    options =
                        listOf(
                            "date_modified" to stringResource(R.string.sort_date_modified),
                            "date_created" to stringResource(R.string.sort_date_created),
                            "name" to stringResource(R.string.sort_name),
                        ),
                    selectedOption = sortBy,
                    onOptionSelect = { sortBy = it },
                    onConfirm = {
                        preferencesManager.setSortBy(sortBy)
                        onSortByChange(sortBy)
                        sortByDialogExpanded = false
                    },
                    onDismiss = { sortByDialogExpanded = false },
                )
            }
            SettingItem(label = stringResource(R.string.autosave), textColor = primaryTextColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = autoBackup,
                        onCheckedChange = {
                            autoBackup = it
                            preferencesManager.setAutoBackup(it)
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (autoBackup) stringResource(R.string.on) else stringResource(R.string.off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryTextColor,
                    )
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAbout() }
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.about_app),
                    style = MaterialTheme.typography.bodyLarge,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.app_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    label: String,
    textColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@Composable
private fun SettingClickableItem(
    label: String,
    value: String,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                options.forEach { (id, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelect(id) }
                                .padding(vertical = 8.dp),
                    ) {
                        RadioButton(
                            selected = id == selectedOption,
                            onClick = { onOptionSelect(id) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.confirm),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
