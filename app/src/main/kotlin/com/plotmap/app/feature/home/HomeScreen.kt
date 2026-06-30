package com.plotmap.app.feature.home
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.BorderCardViolet
import com.plotmap.app.core.designsystem.Gold
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.Lavender
import com.plotmap.app.core.designsystem.LogoTextStyle
import com.plotmap.app.core.designsystem.OnGold
import com.plotmap.app.core.designsystem.Surface
import com.plotmap.app.core.designsystem.TextMuted
import com.plotmap.app.core.designsystem.WineText
import com.plotmap.app.core.designsystem.components.ManuscriptBackground

data class HomeProjectItem(
    val id: String,
    val title: String,
    val description: String,
    val isAiGenerated: Boolean,
    val createdAt: Long,
    val modifiedAt: Long,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listHeightStr: String = "medium",
    projects: List<HomeProjectItem>,
    onNavigateToCreate: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    onRenameProject: (String, String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
) {
    var renameProject by remember { mutableStateOf<HomeProjectItem?>(null) }
    var deleteProject by remember { mutableStateOf<HomeProjectItem?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabTitles =
        listOf(
            stringResource(R.string.project_type_file),
            stringResource(R.string.project_type_manual),
        )

    val filteredProjects =
        projects.filter { project ->
            when (selectedTabIndex) {
                0 -> project.isAiGenerated
                else -> !project.isAiGenerated
            }
        }

    ManuscriptBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { HomeTopLogo() },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = GoldBright,
                            actionIconContentColor = GoldBright,
                        ),
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                Icons.Filled.AccountCircle,
                                contentDescription = stringResource(R.string.profile),
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                SmallFloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = Gold,
                    contentColor = OnGold,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_project))
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    item {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.search_projects), color = TextMuted) },
                            leadingIcon = { Icon(Icons.Filled.Search, null, tint = GoldBright) },
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Surface,
                                    unfocusedContainerColor = Surface,
                                    focusedBorderColor = GoldBright,
                                    unfocusedBorderColor = BorderCardViolet,
                                    cursorColor = GoldBright,
                                ),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = GoldBright,
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    selectedContentColor = GoldBright,
                                    unselectedContentColor = Lavender,
                                    text = { Text(title) },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(items = filteredProjects, key = { it.id }) { project ->
                        ProjectCardItem(
                            title = project.title,
                            description = project.description,
                            listHeightStr = listHeightStr,
                            onClick = { onNavigateToEditor(project.id) },
                            onEditClick = {
                                renameProject = project
                                editedTitle = project.title
                            },
                            onDeleteClick = { deleteProject = project },
                        )
                    }
                }
            }
        }
    }

    if (renameProject != null) {
        AlertDialog(
            onDismissRequest = { renameProject = null },
            title = { Text(text = stringResource(R.string.rename_project)) },
            text = {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.enter_project_name)) },
                    shape = RoundedCornerShape(18.dp),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedTitle = editedTitle.trim()
                        val projectId = renameProject?.id ?: return@TextButton
                        onRenameProject(projectId, updatedTitle)
                        renameProject = null
                    },
                    enabled = editedTitle.any { it.isLetter() },
                ) {
                    Text(text = stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameProject = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    if (deleteProject != null) {
        AlertDialog(
            onDismissRequest = { deleteProject = null },
            title = { Text(text = stringResource(R.string.delete_project)) },
            text = { Text(text = stringResource(R.string.delete_project_confirm, deleteProject?.title ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    val projectId = deleteProject?.id ?: return@TextButton
                    onDeleteProject(projectId)
                    deleteProject = null
                }) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteProject = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun HomeTopLogo() {
    Text(
        text = stringResource(R.string.app_name),
        style = LogoTextStyle.copy(fontSize = 28.sp),
        color = GoldBright,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCardItem(
    title: String,
    description: String,
    listHeightStr: String = "medium",
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val heightDp =
        when (listHeightStr) {
            "small" -> 100.dp
            "large" -> 180.dp
            else -> 140.dp
        }

    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(heightDp)
                .border(1.dp, BorderCardViolet, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                )
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2,
                    )
                }
            }
            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = stringResource(R.string.edit),
                        tint = GoldBright,
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = WineText,
                    )
                }
            }
        }
    }
}
