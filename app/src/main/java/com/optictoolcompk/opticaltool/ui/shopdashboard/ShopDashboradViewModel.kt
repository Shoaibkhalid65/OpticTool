package com.optictoolcompk.opticaltool.ui.shopdashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.models.ShopSettings
import com.optictoolcompk.opticaltool.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopDashboardViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopDashboardUiState())
    val uiState: StateFlow<ShopDashboardUiState> = _uiState.asStateFlow()

    init {
        loadShopSettings()
    }

    private fun loadShopSettings() {
        viewModelScope.launch {
            try {
                repository.getShopSettingsFlow()
                    .catch { e ->
                        e.printStackTrace()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load shop settings"
                            )
                        }
                    }
                    .collect { settings ->
                        _uiState.update {
                            it.copy(
                                shopName = settings.shopName,
                                shopAddress = settings.shopAddress,
                                shopPhone = settings.shopPhone,
                                termsAndConditions = settings.termsAndConditions,
                                currency = settings.currency,
                                isLoading = false,
                                hasChanges = false
                            )
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load shop settings"
                    )
                }
            }
        }
    }

    fun onShopNameChanged(name: String) {
        _uiState.update {
            it.copy(
                shopName = name,
                hasChanges = true
            )
        }
    }

    fun onShopAddressChanged(address: String) {
        _uiState.update {
            it.copy(
                shopAddress = address,
                hasChanges = true
            )
        }
    }

    fun onShopPhoneChanged(phone: String) {
        _uiState.update {
            it.copy(
                shopPhone = phone,
                hasChanges = true
            )
        }
    }

    fun onTermsAndConditionsChanged(terms: String) {
        _uiState.update {
            it.copy(
                termsAndConditions = terms,
                hasChanges = true
            )
        }
    }

    fun onCurrencyChanged(currency: String) {
        _uiState.update {
            it.copy(
                currency = currency,
                hasChanges = true
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                if (!validateSettings()) {
                    return@launch
                }

                _uiState.update { it.copy(isSaving = true) }

                val state = _uiState.value
                val settings = ShopSettings(
                    shopName = state.shopName,
                    shopAddress = state.shopAddress,
                    shopPhone = state.shopPhone,
                    termsAndConditions = state.termsAndConditions,
                    currency = state.currency
                )

                val result = repository.saveShopSettings(settings)

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasChanges = false,
                            saveSuccess = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Failed to save settings"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save settings: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateSettings(): Boolean {
        val state = _uiState.value

        if (state.shopName.isBlank()) {
            _uiState.update { it.copy(error = "Shop name is required") }
            return false
        }

        if (state.shopAddress.isBlank()) {
            _uiState.update { it.copy(error = "Shop address is required") }
            return false
        }

        if (state.currency.isBlank()) {
            _uiState.update { it.copy(error = "Currency is required") }
            return false
        }

        return true
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

data class ShopDashboardUiState(
    val shopName: String = "",
    val shopAddress: String = "",
    val shopPhone: String = "",
    val termsAndConditions: String = "",
    val currency: String = "Rs",

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)