package com.plotmap.app.feature.editor.creation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.components.NextButton
import com.plotmap.app.core.designsystem.components.PlotMapBackButton

@Composable
fun CreationChoiceScreen(
    onCreateProject: (name: String, description: String, sourceText: String, isAiGenerated: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val defaultProjectName = stringResource(R.string.new_project)
    var projectName by remember { mutableStateOf(defaultProjectName) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(false) }
    var projectDescription by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var isFileSelected by remember { mutableStateOf(false) }
    var projectNameInput by remember { mutableStateOf(TextFieldValue(defaultProjectName)) }

    val canProceedToGeneration =
        projectName.any { it.isLetter() } && (selectedType && (isFileSelected || textContent.isNotBlank()) || !selectedType)
    val activeBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
    val inactiveBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun commitProjectName() {
        val normalizedName = projectNameInput.text.trim()
        projectName = if (normalizedName.isNotBlank()) normalizedName else defaultProjectName
        projectNameInput =
            TextFieldValue(
                text = projectName,
                selection = TextRange(projectName.length),
            )
        isEditingTitle = false
        focusManager.clearFocus()
    }

    val context = LocalContext.current
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val content = inputStream.bufferedReader().use { it.readText() }
                        textContent = content
                        isFileSelected = true
                    }
                }
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlotMapBackButton(onClick = onBack, contentDescription = stringResource(R.string.btn_back))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEditingTitle) {
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    OutlinedTextField(
                        value = projectNameInput,
                        onValueChange = {
                            projectNameInput = it
                            projectName = it.text
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium,
                        modifier =
                            Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions =
                            KeyboardActions(
                                onDone = { commitProjectName() },
                            ),
                        shape = RoundedCornerShape(18.dp),
                    )
                } else {
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable {
                                    projectNameInput =
                                        TextFieldValue(
                                            text = projectName,
                                            selection = TextRange(0, projectName.length),
                                        )
                                    isEditingTitle = true
                                },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.project_description),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = projectDescription,
                onValueChange = { newValue ->
                    projectDescription = newValue.take(200)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.project_description_hint)) },
                supportingText = {
                    Text(text = "${projectDescription.length}/200")
                },
                maxLines = 4,
                shape = RoundedCornerShape(18.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.choose_project_type),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))

            ElevatedCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (selectedType) activeBorderColor else inactiveBorderColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { selectedType = true },
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.project_type_file),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ElevatedCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (!selectedType) activeBorderColor else inactiveBorderColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { selectedType = false },
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.project_type_manual),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (selectedType) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.project_source),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { filePickerLauncher.launch("text/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isFileSelected && textContent.isBlank(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text(stringResource(R.string.upload_file))
                    }
                    if (isFileSelected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.file_selected),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = {
                                    isFileSelected = false
                                    textContent = ""
                                },
                                modifier = Modifier.width(100.dp),
                            ) {
                                Text(
                                    stringResource(R.string.clear),
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.or_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                    label = { Text(stringResource(R.string.paste_text_here)) },
                    enabled = !isFileSelected,
                    shape = RoundedCornerShape(18.dp),
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(24.dp))
            NextButton(
                onClick = { onCreateProject(projectName.trim(), projectDescription.trim(), textContent.trim(), selectedType) },
                enabled = canProceedToGeneration,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
