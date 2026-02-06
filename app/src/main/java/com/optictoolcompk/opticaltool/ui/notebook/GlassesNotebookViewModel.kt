package com.optictoolcompk.opticaltool.ui.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.models.ClipboardData
import com.optictoolcompk.opticaltool.data.models.NotebookMode
import com.optictoolcompk.opticaltool.data.models.NotebookSection
import com.optictoolcompk.opticaltool.data.models.NotebookStatistics
import com.optictoolcompk.opticaltool.data.models.SectionOption
import com.optictoolcompk.opticaltool.data.repository.NotebookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlassesNotebookViewModel @Inject constructor(
    private val repository: NotebookRepository
) : ViewModel() {

    // ==================== STATE ====================

    private val _uiState = MutableStateFlow(NotebookUiState(isLoading = true))
    val uiState: StateFlow<NotebookUiState> = _uiState.asStateFlow()

    private val _clipboardData = MutableStateFlow(ClipboardData())
    val clipboardData: StateFlow<ClipboardData> = _clipboardData.asStateFlow()

    private val _sections = MutableStateFlow<List<NotebookSection>>(emptyList())
    val sections: StateFlow<List<NotebookSection>> = _sections.asStateFlow()

    private val _statistics = MutableStateFlow(NotebookStatistics())
    val statistics: StateFlow<NotebookStatistics> = _statistics.asStateFlow()

    private val _selectedSectionId = MutableStateFlow<Long?>(null)
    val selectedSectionId: StateFlow<Long?> = _selectedSectionId.asStateFlow()

    private val _currentClipboardPage = MutableStateFlow(1)

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // ==================== INITIALIZATION ====================

    init {
        initializeData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initializeData() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Initialize DB and load Sections (Critical Path)
            repository.initializeDefaultSectionsIfNeeded()
            
            repository.getAllSections().collect { sectionsList ->
                _sections.value = sectionsList

                if (_selectedSectionId.value == null && sectionsList.isNotEmpty()) {
                    _selectedSectionId.value = sectionsList.first().id
                }
                
                // Once sections are loaded, we can stop the initial screen loading glitch
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            // 2. Load clipboard data (Staggered to reduce initial thread contention)
            _currentClipboardPage.flatMapLatest { page ->
                repository.getClipboardData(page)
            }.collect { data ->
                _clipboardData.value = data
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            // 3. Load statistics
            val stats = repository.getStatistics()
            _statistics.value = stats
        }
    }

    // ==================== SECTION OPERATIONS ====================

    fun selectSection(sectionId: Long?) {
        _selectedSectionId.value = sectionId
    }

    fun selectViewAllSections() {
        _selectedSectionId.value = -1L
    }

    fun createSection(name: String, mode: NotebookMode) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.createSection(name, mode).fold(
                onSuccess = { id ->
                    _selectedSectionId.value = id
                    showToast("Section '$name' created")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateSectionName(sectionId: Long, newName: String) {
        viewModelScope.launch {
            val section = _sections.value.find { it.id == sectionId } ?: return@launch

            repository.updateSection(section.copy(name = newName)).fold(
                onSuccess = {
                    showToast("Section renamed to '$newName'")
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun updateSectionMode(sectionId: Long, newMode: NotebookMode) {
        viewModelScope.launch {
            val section = _sections.value.find { it.id == sectionId } ?: return@launch

            repository.updateSection(section.copy(mode = newMode)).fold(
                onSuccess = { },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun deleteSection(sectionId: Long) {
        viewModelScope.launch {
            val section = _sections.value.find { it.id == sectionId }
            val sectionName = section?.name ?: "Section"

            repository.deleteSection(sectionId).fold(
                onSuccess = {
                    if (_selectedSectionId.value == sectionId) {
                        val remainingSections = _sections.value.filter { it.id != sectionId }
                        _selectedSectionId.value = remainingSections.firstOrNull()?.id
                    }

                    showToast("Section '$sectionName' deleted")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun moveSectionUp(sectionId: Long) {
        viewModelScope.launch {
            repository.moveSectionUp(sectionId).fold(
                onSuccess = { },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun moveSectionDown(sectionId: Long) {
        viewModelScope.launch {
            repository.moveSectionDown(sectionId).fold(
                onSuccess = { },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    // ==================== ROW OPERATIONS ====================

    fun addRow(sectionId: Long, sphValue: String, cylValue: String, pairs: Int, mode: NotebookMode) {
        viewModelScope.launch {
            repository.addRow(sectionId, sphValue, cylValue, pairs, mode).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun toggleCopyFlag(rowId: Long) {
        viewModelScope.launch {
            repository.toggleCopyFlag(rowId).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun toggleOrderedFlag(rowId: Long) {
        viewModelScope.launch {
            repository.toggleOrderedFlag(rowId).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun toggleDeleteFlag(rowId: Long) {
        viewModelScope.launch {
            repository.toggleDeleteFlag(rowId).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun deleteRow(rowId: Long) {
        viewModelScope.launch {
            repository.deleteRow(rowId).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    // ==================== CLIPBOARD OPERATIONS ====================

    fun clearClipboard() {
        viewModelScope.launch {
            repository.clearClipboard().fold(
                onSuccess = {
                    showToast("Clipboard cleared")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun markClipboardAsOrdered() {
        viewModelScope.launch {
            repository.markClipboardAsOrdered().fold(
                onSuccess = {
                    val count = _clipboardData.value.totalRows
                    showToast("Marked $count rows as ordered")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun goToClipboardPage(page: Int) {
        _currentClipboardPage.value = page.coerceIn(1, _clipboardData.value.totalPages)
    }

    fun goToPreviousClipboardPage() {
        if (_currentClipboardPage.value > 1) {
            _currentClipboardPage.value -= 1
        }
    }

    fun goToNextClipboardPage() {
        if (_currentClipboardPage.value < _clipboardData.value.totalPages) {
            _currentClipboardPage.value += 1
        }
    }

    // ==================== BULK OPERATIONS ====================

    fun deleteCopiedRowsInSection(sectionId: Long) {
        viewModelScope.launch {
            val section = _sections.value.find { it.id == sectionId }
            val count = section?.copiedRowsCount ?: 0

            if (count == 0) {
                showToast("No copied rows to delete")
                return@launch
            }

            repository.deleteCopiedRowsInSection(sectionId).fold(
                onSuccess = {
                    showToast("Deleted $count copied rows")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun deleteOrderedRowsInSection(sectionId: Long) {
        viewModelScope.launch {
            val section = _sections.value.find { it.id == sectionId }
            val count = section?.orderedRowsCount ?: 0

            if (count == 0) {
                showToast("No ordered rows to delete")
                return@launch
            }

            repository.deleteOrderedRowsInSection(sectionId).fold(
                onSuccess = {
                    showToast("Deleted $count ordered rows")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun deleteMarkedRowsInSection(sectionId: Long) {
        viewModelScope.launch {
            repository.deleteMarkedRowsInSection(sectionId).fold(
                onSuccess = {
                    showToast("Deleted marked rows")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun markAllCopyInSection(sectionId: Long, isCopy: Boolean) {
        viewModelScope.launch {
            repository.markAllCopyInSection(sectionId, isCopy).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun markAllOrderedInSection(sectionId: Long, isOrdered: Boolean) {
        viewModelScope.launch {
            repository.markAllOrderedInSection(sectionId, isOrdered).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    fun markAllDeleteInSection(sectionId: Long, isDelete: Boolean) {
        viewModelScope.launch {
            repository.markAllDeleteInSection(sectionId, isDelete).fold(
                onSuccess = {
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )
        }
    }

    // ==================== RESET OPERATIONS ====================

    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.resetToDefault().fold(
                onSuccess = {
                    _selectedSectionId.value = _sections.value.firstOrNull()?.id
                    showToast("Reset to default sections")
                    refreshStatistics()
                },
                onFailure = { error ->
                    showToast("Error: ${error.message}")
                }
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ==================== HELPER METHODS ====================

    private suspend fun refreshStatistics() {
        _statistics.value = repository.getStatistics()
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    fun getSelectedSection(): NotebookSection? {
        val selectedId = _selectedSectionId.value ?: return null
        if (selectedId == -1L) return null
        return _sections.value.find { it.id == selectedId }
    }

    fun getSectionOptions(): List<SectionOption> {
        val options = _sections.value.map {
            SectionOption(id = it.id, name = it.name)
        }.toMutableList()

        if (options.isNotEmpty()) {
            options.add(SectionOption(id = -1L, name = "View All Sections", isViewAll = true))
        }

        return options
    }
}

data class NotebookUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
