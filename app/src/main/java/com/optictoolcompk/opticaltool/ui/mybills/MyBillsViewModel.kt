package com.optictoolcompk.opticaltool.ui.mybills


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.models.Bill
import com.optictoolcompk.opticaltool.data.models.BillFilter
import com.optictoolcompk.opticaltool.data.models.BillSortOption
import com.optictoolcompk.opticaltool.data.models.BillStatistics
import com.optictoolcompk.opticaltool.data.models.ShopSettings
import com.optictoolcompk.opticaltool.data.repository.BillRepository
import com.optictoolcompk.opticaltool.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBillsViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBillsUiState())
    val uiState: StateFlow<MyBillsUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(BillFilter())
    val filterState: StateFlow<BillFilter> = _filterState.asStateFlow()

    private val _shopSettings = MutableStateFlow(ShopSettings())
    val shopSettings: StateFlow<ShopSettings> = _shopSettings.asStateFlow()

    init {
        loadBills()
        loadStatistics()
        loadShopSettings()
        observeBillChanges()
    }

    private fun loadBills() {
        viewModelScope.launch {
            repository.getAllBillsFlow()
                .combine(_filterState) { bills, filter ->
                    applyFilters(bills, filter)
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load bills"
                        )
                    }
                }
                .collect { filteredBills ->
                    _uiState.update {
                        it.copy(
                            bills = filteredBills,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = repository.getBillStatistics()
                _uiState.update { it.copy(statistics = stats) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadShopSettings() {
        viewModelScope.launch {
            repository.getShopSettingsFlow()
                .catch { e ->
                    e.printStackTrace()
                }
                .collect { settings ->
                    _shopSettings.value = settings
                }
        }
    }

    private fun observeBillChanges() {
        viewModelScope.launch {
            repository.getAllBillsFlow()
                .collect { bills ->
                    // Every time bills change, refresh statistics
                    loadStatistics()
                }
        }
    }

    private fun applyFilters(bills: List<Bill>, filter: BillFilter): List<Bill> {
        var filtered = bills

        // Search query
        if (filter.searchQuery.isNotBlank()) {
            val query = filter.searchQuery.lowercase()
            filtered = filtered.filter {
                it.invoiceNumber.lowercase().contains(query) ||
                        it.customerName.lowercase().contains(query) ||
                        it.customerPhone.contains(query) ||
                        it.invoiceDate.lowercase().contains(query)
            }
        }

        // Date range filter
        if (filter.startDate != null || filter.endDate != null) {
            filtered = filtered.filter { bill ->
                DateTimeUtils.isDateInRange(
                    bill.invoiceDate,
                    filter.startDate,
                    filter.endDate
                )
            }
        }

        // Show only unpaid filter
        if (filter.showOnlyUnpaid) {
            filtered = filtered.filter { it.isUnpaid }
        }

        // Sort
        filtered = when (filter.sortBy) {
            BillSortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.createdAt }
            BillSortOption.OLDEST_FIRST -> filtered.sortedBy { it.createdAt }
            BillSortOption.AMOUNT_HIGH_TO_LOW -> filtered.sortedByDescending { it.totalAmount }
            BillSortOption.AMOUNT_LOW_TO_HIGH -> filtered.sortedBy { it.totalAmount }
            BillSortOption.UNPAID_ONLY -> filtered.filter { it.isUnpaid }
                .sortedByDescending { it.createdAt }
        }

        return filtered
    }

    fun onSearchQueryChanged(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun onStartDateChanged(date: String?) {
        _filterState.update { it.copy(startDate = date) }
    }

    fun onEndDateChanged(date: String?) {
        _filterState.update { it.copy(endDate = date) }
    }

    fun onSortOptionChanged(option: BillSortOption) {
        _filterState.update { it.copy(sortBy = option) }
    }

    fun onShowOnlyUnpaidChanged(showOnlyUnpaid: Boolean) {
        _filterState.update { it.copy(showOnlyUnpaid = showOnlyUnpaid) }
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBill(billId)
                loadStatistics() // Refresh statistics after deletion
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        error = "Failed to delete bill: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class MyBillsUiState(
    val bills: List<Bill> = emptyList(),
    val statistics: BillStatistics = BillStatistics(),
    val isLoading: Boolean = true,
    val error: String? = null
)