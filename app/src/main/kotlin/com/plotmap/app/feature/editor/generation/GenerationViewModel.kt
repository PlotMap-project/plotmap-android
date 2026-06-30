package com.plotmap.app.feature.editor.generation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plotmap.app.core.data.GenerationFailedException
import com.plotmap.app.core.data.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
sealed interface GenerationUiState {
    data object Loading : GenerationUiState

    data class Success(val projectId: String) : GenerationUiState

    data class Error(val message: String?) : GenerationUiState
}

class GenerationViewModel(
    private val projectRepository: ProjectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<GenerationUiState>(GenerationUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun generate(
        name: String,
        description: String,
        text: String,
    ) {
        viewModelScope.launch {
            _uiState.value = GenerationUiState.Loading
            runCatching { projectRepository.generateGraph(name, description, text) }
                .onSuccess { graph -> _uiState.value = GenerationUiState.Success(graph.projectId) }
                .onFailure { throwable ->
                    Log.e("GenerationVM", "Ошибка при генерации графа", throwable)
                    val errorMessage = when (throwable) {
                        is GenerationFailedException -> throwable.reason
                        is retrofit2.HttpException -> {
                            throwable.response()?.errorBody()?.string() ?: throwable.message
                        }
                        else -> throwable.message
                    }

                    _uiState.value = GenerationUiState.Error(errorMessage)
                }
        }
    }
}
