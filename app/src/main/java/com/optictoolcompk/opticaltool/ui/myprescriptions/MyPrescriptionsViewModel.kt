package com.optictoolcompk.opticaltool.ui.myprescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.models.PrescriptionEntity
import com.optictoolcompk.opticaltool.data.models.PrescriptionFilter
import com.optictoolcompk.opticaltool.data.models.PrescriptionSortOption
import com.optictoolcompk.opticaltool.data.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class PrescriptionListViewModel @Inject constructor(
    private val repository: PrescriptionRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(PrescriptionFilter())
    val filterState: StateFlow<PrescriptionFilter> = _filterState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    val prescriptions: StateFlow<List<PrescriptionEntity>> = repository.getAllPrescriptions()
        .combine(_filterState) { prescriptions, filter ->
            applyFiltersAndSort(prescriptions, filter)
        }
        .catch { e ->
            e.printStackTrace()
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun applyFiltersAndSort(
        prescriptions: List<PrescriptionEntity>,
        filter: PrescriptionFilter
    ): List<PrescriptionEntity> {
        var filtered = prescriptions

        // Apply Search
        if (filter.searchQuery.isNotBlank()) {
            val query = filter.searchQuery.lowercase()
            filtered = filtered.filter {
                it.patientName.lowercase().contains(query) ||
                        it.phone.contains(query) ||
                        it.prescriptionNumber.lowercase().contains(query) ||
                        it.city.lowercase().contains(query)
            }
        }

        // Apply Sort
        filtered = when (filter.sortBy) {
            PrescriptionSortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.createdAt }
            PrescriptionSortOption.OLDEST_FIRST -> filtered.sortedBy { it.createdAt }
            PrescriptionSortOption.NUMBER_ASC -> filtered.sortedBy { it.prescriptionNumber }
            PrescriptionSortOption.NUMBER_DESC -> filtered.sortedByDescending { it.prescriptionNumber }
        }

        return filtered
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun updateSortOption(option: PrescriptionSortOption) {
        _filterState.update { it.copy(sortBy = option) }
    }

    fun deletePrescription(prescription: PrescriptionEntity) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Deleting
            val result = repository.deletePrescription(prescription)
            _deleteState.value = if (result.isSuccess) {
                DeleteState.Success
            } else {
                DeleteState.Error(result.exceptionOrNull()?.message ?: "Failed to delete")
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Deleting : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}
