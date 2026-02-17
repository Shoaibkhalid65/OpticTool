//package com.optictoolcompk.opticaltool.ui.billcreation
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.net.Uri
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.optictoolcompk.opticaltool.data.models.*
//import com.optictoolcompk.opticaltool.data.repository.BillRepository
//import com.optictoolcompk.opticaltool.data.repository.PrescriptionRepository
//import com.optictoolcompk.opticaltool.utils.BillImageStorage
//import com.optictoolcompk.opticaltool.utils.DateTimeUtils
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.FlowPreview
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import javax.inject.Inject
//
//@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
//@HiltViewModel
//class BillCreationViewModel @Inject constructor(
//    private val billRepository: BillRepository,
//    private val prescriptionRepository: PrescriptionRepository,
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    private val billId: Long? = savedStateHandle.get<Long>("billId")
//
//    private val _uiState = MutableStateFlow(BillCreationUiState(initialLoading = true))
//    val uiState: StateFlow<BillCreationUiState> = _uiState.asStateFlow()
//
//    private val _shopSettings = MutableStateFlow(ShopSettings())
//    val shopSettings: StateFlow<ShopSettings> = _shopSettings.asStateFlow()
//
//    private val _displaySettings = MutableStateFlow(BillDisplaySettings())
//    val displaySettings: StateFlow<BillDisplaySettings> = _displaySettings.asStateFlow()
//
//    // Unpaid bills search
//    private val _showUnpaidBillsDialog = MutableStateFlow(false)
//    val showUnpaidBillsDialog: StateFlow<Boolean> = _showUnpaidBillsDialog.asStateFlow()
//
//    private val _unpaidBillsSearchQuery = MutableStateFlow("")
//    val unpaidBillsSearchQuery: StateFlow<String> = _unpaidBillsSearchQuery.asStateFlow()
//
//    val unpaidBillsSearchResults: StateFlow<List<Bill>> =
//        combine(
//            _unpaidBillsSearchQuery
//                .debounce(300)
//                .flatMapLatest { query ->
//                    if (query.length >= 2) {
//                        billRepository.searchUnpaidBills(query)
//                    } else {
//                        flowOf(emptyList())
//                    }
//                },
//            uiState
//        ) { bills, state ->
//            bills.filterNot { it.id in state.selectedUnpaidBills }
//        }
//            .stateIn(
//                viewModelScope,
//                SharingStarted.WhileSubscribed(5_000),
//                emptyList()
//            )
//
//    // Prescription search
//    private val _showPrescriptionSearchDialog = MutableStateFlow(false)
//    val showPrescriptionSearchDialog: StateFlow<Boolean> =
//        _showPrescriptionSearchDialog.asStateFlow()
//
//    private val _prescriptionSearchQuery = MutableStateFlow("")
//    val prescriptionSearchQuery: StateFlow<String> = _prescriptionSearchQuery.asStateFlow()
//
//    val prescriptionSearchResults: StateFlow<List<PrescriptionEntity>> =
//        combine(
//            _prescriptionSearchQuery
//                .debounce(300)
//                .flatMapLatest { query ->
//                    if (query.length >= 2) {
//                        prescriptionRepository.searchPrescriptions(query)
//                    } else {
//                        flowOf(emptyList())
//                    }
//                },
//            uiState
//        ) { prescriptions, state ->
//            prescriptions.filterNot { it.prescriptionImagePath in state.prescriptionImagesPaths }
//        }
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    init {
//        initializeViewModel()
//    }
//
//    private fun initializeViewModel() {
//        viewModelScope.launch {
//            // Load shop settings first
//            val settings = withContext(Dispatchers.IO) {
//                billRepository.getShopSettings()
//            }
//            _shopSettings.value = settings
//
//            if (billId != null && billId > 0) {
//                loadBillForEdit(billId)
//            } else {
//                generateInvoiceNumber()
//                _uiState.update { it.copy(initialLoading = false) }
//            }
//        }
//    }
//
//    private fun generateInvoiceNumber() {
//        viewModelScope.launch {
//            val nextNumber = billRepository.getNextInvoiceNumber()
//            _uiState.update { it.copy(invoiceNumber = nextNumber) }
//        }
//    }
//
//    suspend fun loadBillForEdit(id: Long) {
//        try {
//            val bill = withContext(Dispatchers.IO) {
//                billRepository.getBillById(id)
//            }
//
//            if (bill != null) {
//                _uiState.update {
//                    it.copy(
//                        initialLoading = false,
//                        isEditMode = true,
//                        invoiceNumber = bill.invoiceNumber,
//                        invoiceDate = bill.invoiceDate,
//                        invoiceTime = bill.invoiceTime,
//                        customerName = bill.customerName,
//                        customerPhone = bill.customerPhone,
//                        customerCity = bill.customerCity,
//                        items = bill.items,
//                        totalAmount = bill.totalAmount,
//                        discount = bill.discount.toString(),
//                        advance = bill.advance.toString(),
//                        advance2 = bill.advance2.toString(),
//                        advance2Date = bill.advance2Date,
//                        advance3 = bill.advance3.toString(),
//                        advance3Date = bill.advance3Date,
//                        previousAmount = bill.previousAmount,
//                        remainingAmount = bill.remainingAmount,
//                        remainingNote = bill.remainingNote,
//                        pickupDate = bill.pickupDate,
//                        imagePaths = bill.imagesPaths,
//                        prescription = bill.prescription,
//                        prescriptionImagesPaths = bill.prescriptionImagesPaths,
//                        prescriptionFormData = bill.prescriptionFormData,
//                        showPrescriptionFormCard = bill.prescriptionFormData != null && bill.prescriptionFormData.patientName.isNotBlank(),
//                        prescriptionFormInputs = bill.prescriptionFormData ?: PrescriptionFormDataForBill()
//                    )
//                }
//            } else {
//                _uiState.update { it.copy(initialLoading = false, error = "Bill not found") }
//            }
//        } catch (e: Exception) {
//            _uiState.update { it.copy(initialLoading = false, error = "Failed to load bill: ${e.message}") }
//        }
//    }
//
//    // Customer info
//    fun onCustomerNameChanged(name: String) {
//        _uiState.update { it.copy(customerName = name) }
//    }
//
//    fun onCustomerPhoneChanged(phone: String) {
//        _uiState.update { it.copy(customerPhone = phone) }
//    }
//
//    fun onCustomerCityChanged(city: String) {
//        _uiState.update { it.copy(customerCity = city) }
//    }
//
//    // Invoice date/time
//    fun onInvoiceDateChanged(date: String) {
//        _uiState.update { it.copy(invoiceDate = date) }
//    }
//
//    fun onInvoiceTimeChanged(time: String) {
//        _uiState.update { it.copy(invoiceTime = time) }
//    }
//
//    // Items management
//    fun onAddItem() {
//        val currentItems = _uiState.value.items.toMutableList()
//        currentItems.add(BillItem())
//        _uiState.update { it.copy(items = currentItems) }
//    }
//
//    fun onRemoveItem(index: Int) {
//        val currentItems = _uiState.value.items.toMutableList()
//        if (index in currentItems.indices) {
//            currentItems.removeAt(index)
//            _uiState.update { it.copy(items = currentItems) }
//            calculateTotals()
//        }
//    }
//
//    fun onItemChanged(index: Int, item: BillItem) {
//        val currentItems = _uiState.value.items.toMutableList()
//        if (index in currentItems.indices) {
//            currentItems[index] = item.copy(
//                quantity = item.quantity,
//                price = item.price
//            )
//            _uiState.update { it.copy(items = currentItems) }
//            calculateTotals()
//        }
//    }
//
//    // ==================== PRESCRIPTION MANAGEMENT ====================
//
//    fun onShowPrescriptionOptionsDialog() {
//        if (_uiState.value.prescriptionFormData != null || _uiState.value.showPrescriptionFormCard) {
//            _uiState.update { it.copy(error = "You can only add one new prescription per bill") }
//            return
//        }
//        _uiState.update { it.copy(showPrescriptionOptionsDialog = true) }
//    }
//
//    fun onHidePrescriptionOptionsDialog() {
//        _uiState.update { it.copy(showPrescriptionOptionsDialog = false) }
//    }
//
//    fun onAddSavedPrescriptionImage(imagePath: String) {
//        val currentImages = _uiState.value.prescriptionImagesPaths.toMutableList()
//        if (currentImages.size < 3) {
//            currentImages.add(imagePath)
//            _uiState.update { it.copy(prescriptionImagesPaths = currentImages) }
//            onHidePrescriptionSearchDialog()
//        }
//    }
//
//    fun onRemovePrescriptionImage(index: Int) {
//        val currentImages = _uiState.value.prescriptionImagesPaths.toMutableList()
//        if (index in currentImages.indices) {
//            val removedPath = currentImages.removeAt(index)
//            _uiState.update {
//                it.copy(
//                    prescriptionImagesPaths = currentImages,
//                    newPrescriptionImagesToAutoSave = it.newPrescriptionImagesToAutoSave - removedPath
//                )
//            }
//        }
//    }
//
//    fun onPrescriptionImageSelected(context: Context, uri: Uri) {
//        viewModelScope.launch {
//            try {
//                _uiState.update { it.copy(isUploadingImages = true) }
//
//                val result = BillImageStorage.saveImage(context, uri)
//                if (result.isSuccess) {
//                    val imagePath = result.getOrThrow()
//                    val nextNo = getNextPrescriptionNumber()
//
//                    val formData = PrescriptionFormDataForBill(
//                        prescriptionNumber = nextNo,
//                        prescriptionImagePath = imagePath
//                    )
//
//                    _uiState.update {
//                        it.copy(
//                            prescriptionFormData = formData,
//                            newPrescriptionImagesToAutoSave = it.newPrescriptionImagesToAutoSave + imagePath,
//                            isUploadingImages = false
//                        )
//                    }
//                }
//
//                onHidePrescriptionOptionsDialog()
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isUploadingImages = false,
//                        error = "Failed to add prescription image: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
//
//    fun onShowPrescriptionForm() {
//        if (_uiState.value.prescriptionFormData != null) {
//            _uiState.update { it.copy(error = "You can only add one new prescription per bill") }
//            return
//        }
//        viewModelScope.launch {
//            val nextNo = getNextPrescriptionNumber()
//            _uiState.update {
//                it.copy(
//                    showPrescriptionFormCard = true,
//                    showPrescriptionOptionsDialog = false,
//                    prescriptionFormInputs = it.prescriptionFormInputs.copy(prescriptionNumber = nextNo)
//                )
//            }
//        }
//    }
//
//    fun onHidePrescriptionForm() {
//        _uiState.update { it.copy(showPrescriptionFormCard = false) }
//    }
//
//    fun onPrescriptionFormInputChanged(inputs: PrescriptionFormDataForBill) {
//        _uiState.update { it.copy(prescriptionFormInputs = inputs) }
//    }
//
//    fun onSavePrescriptionForm(
//        context: Context,
//        prescriptionBitmap: Bitmap
//    ) {
//        viewModelScope.launch {
//            try {
//                _uiState.update { it.copy(isCapturingPrescriptionForm = true) }
//
//                val formData = _uiState.value.prescriptionFormInputs
//                val prescriptionNumber = formData.prescriptionNumber
//
//                val imagePath = BillImageStorage.savePrescriptionImage(
//                    context,
//                    prescriptionBitmap,
//                    prescriptionNumber
//                )
//
//                val updatedFormData = formData.copy(
//                    prescriptionImagePath = imagePath
//                )
//
//                _uiState.update {
//                    it.copy(
//                        prescriptionFormData = updatedFormData,
//                        isCapturingPrescriptionForm = false,
//                        prescriptionFormCaptureBitmap = prescriptionBitmap // Store bitmap for auto-save
//                    )
//                }
//
//                performSaveBill()
//
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isCapturingPrescriptionForm = false,
//                        error = "Failed to save prescription form: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun savePrescriptionToDatabase(
//        prescriptionNumber: String,
//        formData: PrescriptionFormDataForBill,
//        bitmap: Bitmap? = null,
//        imagePath: String? = null
//    ) {
//        try {
//            if (formData.patientName.isNotBlank() && bitmap != null) {
//                prescriptionRepository.createPrescription(
//                    prescriptionNumber = prescriptionNumber,
//                    patientName = formData.patientName,
//                    phone = formData.phone,
//                    age = formData.age,
//                    city = formData.city,
//                    rightSph = formData.rightSph,
//                    rightCyl = formData.rightCyl,
//                    rightAxis = formData.rightAxis,
//                    rightVa = formData.rightVa,
//                    leftSph = formData.leftSph,
//                    leftCyl = formData.leftCyl,
//                    leftAxis = formData.leftAxis,
//                    leftVa = formData.leftVa,
//                    add = formData.addPower,
//                    ipdN = formData.ipdNear,
//                    ipdD = formData.ipdDistance,
//                    checkedBy = formData.checkedBy,
//                    prescriptionBitmap = bitmap
//                )
//            } else if (imagePath != null) {
//                prescriptionRepository.createImageOnlyPrescription(
//                    prescriptionNumber = prescriptionNumber,
//                    imageFile = java.io.File(imagePath)
//                )
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun onClearPrescriptionForm() {
//        _uiState.update {
//            it.copy(
//                prescriptionFormData = null,
//                showPrescriptionFormCard = false,
//                prescriptionFormInputs = PrescriptionFormDataForBill(),
//                newPrescriptionImagesToAutoSave = emptySet(),
//                prescriptionFormCaptureBitmap = null
//            )
//        }
//    }
//
//    suspend fun getNextPrescriptionNumber(): String {
//        return prescriptionRepository.getNextPrescriptionNumber()
//    }
//
//    // Amounts
//    fun onDiscountChanged(discount: String) {
//        _uiState.update { it.copy(discount = discount) }
//        calculateTotals()
//    }
//
//    fun onAdvanceChanged(advance: String) {
//        _uiState.update { it.copy(advance = advance) }
//        calculateTotals()
//    }
//
//    fun onAdvance2Changed(advance2: String) {
//        _uiState.update {
//            it.copy(
//                advance2 = advance2,
//                advance2Date = if (advance2.isNotBlank() && it.advance2Date == null) {
//                    DateTimeUtils.getCurrentDate()
//                } else if (advance2.isBlank()) {
//                    null
//                } else {
//                    it.advance2Date
//                }
//            )
//        }
//        calculateTotals()
//    }
//
//    fun onAdvance2DateChanged(date: String?) {
//        _uiState.update { it.copy(advance2Date = date) }
//    }
//
//    fun onAdvance3Changed(advance3: String) {
//        _uiState.update {
//            it.copy(
//                advance3 = advance3,
//                advance3Date = if (advance3.isNotBlank() && it.advance3Date == null) {
//                    DateTimeUtils.getCurrentDate()
//                } else if (advance3.isBlank()) {
//                    null
//                } else {
//                    it.advance3Date
//                }
//            )
//        }
//        calculateTotals()
//    }
//
//    fun onAdvance3DateChanged(date: String?) {
//        _uiState.update { it.copy(advance3Date = date) }
//    }
//
//    private fun calculateTotals() {
//        val total = _uiState.value.items.sumOf { it.total }
//        val discount = _uiState.value.discount.toDoubleOrNull() ?: 0.0
//        val advance = _uiState.value.advance.toDoubleOrNull() ?: 0.0
//        val advance2 = _uiState.value.advance2.toDoubleOrNull() ?: 0.0
//        val advance3 = _uiState.value.advance3.toDoubleOrNull() ?: 0.0
//        val previous = _uiState.value.previousAmount
//
//        val remaining = total - discount - advance - advance2 - advance3 + previous
//
//        _uiState.update {
//            it.copy(
//                totalAmount = total,
//                remainingAmount = remaining
//            )
//        }
//    }
//
//    fun onPickupDateChanged(date: String?) {
//        _uiState.update { it.copy(pickupDate = date) }
//    }
//
//    // Images
//    suspend fun onImagesSelected(context: Context, uris: List<Uri>) {
//        val currentImages = _uiState.value.imagePaths.toMutableList()
//        if (currentImages.size >= 4) return
//
//        _uiState.update { it.copy(isUploadingImages = true) }
//
//        try {
//            val remainingSlots = 4 - currentImages.size
//            val urisToProcess = uris.take(remainingSlots)
//
//            val result = BillImageStorage.saveImages(context, urisToProcess)
//            if (result.isSuccess) {
//                currentImages.addAll(result.getOrThrow())
//                _uiState.update {
//                    it.copy(
//                        imagePaths = currentImages,
//                        isUploadingImages = false
//                    )
//                }
//            } else {
//                _uiState.update {
//                    it.copy(
//                        isUploadingImages = false,
//                        error = "Failed to save images"
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            _uiState.update {
//                it.copy(
//                    isUploadingImages = false,
//                    error = "Failed to upload images: ${e.message}"
//                )
//            }
//        }
//    }
//
//    fun onImageRemoved(imagePath: String) {
//        val currentImages = _uiState.value.imagePaths.toMutableList()
//        currentImages.remove(imagePath)
//        _uiState.update { it.copy(imagePaths = currentImages) }
//
//        viewModelScope.launch {
//            BillImageStorage.deleteImage(imagePath)
//        }
//    }
//
//    // Display settings
//    fun onDisplaySettingsChanged(settings: BillDisplaySettings) {
//        _displaySettings.value = settings
//    }
//
//    // Unpaid bills search
//    fun onShowUnpaidBillsDialog() {
//        _showUnpaidBillsDialog.value = true
//        _unpaidBillsSearchQuery.value = ""
//    }
//
//    fun onHideUnpaidBillsDialog() {
//        _showUnpaidBillsDialog.value = false
//        _unpaidBillsSearchQuery.value = ""
//    }
//
//    fun onUnpaidBillsSearchQueryChanged(query: String) {
//        _unpaidBillsSearchQuery.value = query
//    }
//
//    fun onUnpaidBillSelected(bill: Bill) {
//        _uiState.update {
//            it.copy(
//                previousAmount = it.previousAmount + bill.remainingAmount,
//                selectedUnpaidBills = it.selectedUnpaidBills + bill.id,
//                selectedInvoiceNumbers = it.selectedInvoiceNumbers + bill.invoiceNumber
//            )
//        }
//        calculateTotals()
//        onHideUnpaidBillsDialog()
//    }
//
//    private fun buildCurrentBillRemainingNote(): String? {
//        val invoices = _uiState.value.selectedInvoiceNumbers
//        if (invoices.isEmpty()) return _uiState.value.remainingNote
//
//        return "Includes unpaid invoices: ${invoices.joinToString(", ")}"
//    }
//
//    // Prescription search
//    fun onShowPrescriptionSearchDialog() {
//        _showPrescriptionSearchDialog.value = true
//        _prescriptionSearchQuery.value = ""
//    }
//
//    fun onHidePrescriptionSearchDialog() {
//        _showPrescriptionSearchDialog.value = false
//        _prescriptionSearchQuery.value = ""
//    }
//
//    fun onPrescriptionSearchQueryChanged(query: String) {
//        _prescriptionSearchQuery.value = query
//    }
//
//    fun saveBill() {
//        viewModelScope.launch {
//            try {
//                if (_uiState.value.customerName.isBlank()) {
//                    _uiState.update { it.copy(error = "Customer name is required") }
//                    return@launch
//                }
//
//                if (_uiState.value.customerPhone.isBlank()) {
//                    _uiState.update { it.copy(error = "Customer phone is required") }
//                    return@launch
//                }
//
//                val validItems = _uiState.value.items.filter {
//                    it.itemName.isNotBlank() && it.quantity > 0 && it.price > 0
//                }
//
//                if (validItems.isEmpty()) {
//                    _uiState.update { it.copy(error = "At least one valid item is required") }
//                    return@launch
//                }
//
//                // Validation for prescription form if it's shown
//                if (_uiState.value.showPrescriptionFormCard) {
//                    if (_uiState.value.prescriptionFormInputs.patientName.isBlank()) {
//                        _uiState.update { it.copy(error = "Patient name on prescription is required") }
//                        return@launch
//                    }
//                }
//
//                // If form is visible, we need to capture it first
//                if (_uiState.value.showPrescriptionFormCard) {
//                    _uiState.update { it.copy(shouldTriggerCapture = true) }
//                } else {
//                    performSaveBill()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _uiState.update { it.copy(error = "Error: ${e.message}") }
//            }
//        }
//    }
//
//    private suspend fun performSaveBill() {
//        _uiState.update { it.copy(isSaving = true, shouldTriggerCapture = false) }
//
//        val state = _uiState.value
//
//        // Auto-save logic for the ONE new prescription added
//        if (_displaySettings.value.autoSavePrescriptions && state.prescriptionFormData != null) {
//            val formData = state.prescriptionFormData
//
//            // Check if it was added by image (present in newPrescriptionImagesToAutoSave)
//            if (state.newPrescriptionImagesToAutoSave.contains(formData.prescriptionImagePath)) {
//                savePrescriptionToDatabase(
//                    prescriptionNumber = formData.prescriptionNumber,
//                    formData = formData,
//                    imagePath = formData.prescriptionImagePath
//                )
//            }
//            // Check if it was added by form (patientName is not blank)
//            else if (formData.patientName.isNotBlank() && state.prescriptionFormCaptureBitmap != null) {
//                savePrescriptionToDatabase(
//                    prescriptionNumber = formData.prescriptionNumber,
//                    formData = formData,
//                    bitmap = state.prescriptionFormCaptureBitmap
//                )
//            }
//        }
//
//        val bill = Bill(
//            id = billId ?: 0,
//            invoiceNumber = state.invoiceNumber,
//            invoiceDate = state.invoiceDate,
//            invoiceTime = state.invoiceTime,
//            customerName = state.customerName,
//            customerPhone = state.customerPhone,
//            customerCity = state.customerCity,
//            items = state.items.filter { it.itemName.isNotBlank() },
//            totalAmount = state.totalAmount,
//            discount = state.discount.toDoubleOrNull() ?: 0.0,
//            advance = state.advance.toDoubleOrNull() ?: 0.0,
//            advance2 = state.advance2.toDoubleOrNull() ?: 0.0,
//            advance2Date = state.advance2Date,
//            advance3 = state.advance3.toDoubleOrNull() ?: 0.0,
//            advance3Date = state.advance3Date,
//            previousAmount = state.previousAmount,
//            remainingAmount = state.remainingAmount,
//            remainingNote = buildCurrentBillRemainingNote(),
//            pickupDate = state.pickupDate,
//            prescriptionImagesPaths = state.prescriptionImagesPaths,
//            prescriptionFormData = state.prescriptionFormData,
//            imagesPaths = state.imagePaths
//        )
//
//        val result = if (state.isEditMode && billId != null) {
//            billRepository.updateBill(bill)
//        } else {
//            billRepository.createBill(bill)
//        }
//
//        if (result.isSuccess) {
//            if (state.selectedUnpaidBills.isNotEmpty()) {
//                billRepository.settleBills(
//                    billIds = state.selectedUnpaidBills,
//                    newInvoiceNumber = state.invoiceNumber
//                )
//            }
//
//            _uiState.update {
//                it.copy(
//                    isSaving = false,
//                    saveSuccess = true,
//                )
//            }
//        } else {
//            _uiState.update {
//                it.copy(
//                    isSaving = false,
//                    error = "Failed to save bill"
//                )
//            }
//        }
//    }
//
//    fun clearError() {
//        _uiState.update { it.copy(error = null) }
//    }
//
//    fun clearSaveSuccess() {
//        _uiState.update { it.copy(saveSuccess = false) }
//    }
//}
//
//data class BillCreationUiState(
//    val initialLoading: Boolean = false,
//    val isEditMode: Boolean = false,
//    val invoiceNumber: String = "",
//    val invoiceDate: String = DateTimeUtils.getCurrentDate(),
//    val invoiceTime: String = DateTimeUtils.getCurrentTime(),
//    val customerName: String = "",
//    val customerPhone: String = "",
//    val customerCity: String = "",
//    val items: List<BillItem> = listOf(BillItem()),
//    val totalAmount: Double = 0.0,
//    val discount: String = "",
//    val advance: String = "",
//    val advance2: String = "",
//    val advance2Date: String? = null,
//    val advance3: String = "",
//    val advance3Date: String? = null,
//    val previousAmount: Double = 0.0,
//    val remainingAmount: Double = 0.0,
//    val remainingNote: String? = null,
//    val selectedUnpaidBills: Set<Long> = emptySet(),
//    val selectedInvoiceNumbers: List<String> = emptyList(),
//    val pickupDate: String? = null,
//    val imagePaths: List<String> = emptyList(),
//    val prescription: PrescriptionData? = null,
//    val isSaving: Boolean = false,
//    val isUploadingImages: Boolean = false,
//    val saveSuccess: Boolean = false,
//    val error: String? = null,
//    val prescriptionImagesPaths: List<String> = emptyList(),
//    val newPrescriptionImagesToAutoSave: Set<String> = emptySet(),
//    val prescriptionFormData: PrescriptionFormDataForBill? = null,
//    val prescriptionFormInputs: PrescriptionFormDataForBill = PrescriptionFormDataForBill(),
//    val showPrescriptionOptionsDialog: Boolean = false,
//    val showPrescriptionFormCard: Boolean = false,
//    val isCapturingPrescriptionForm: Boolean = false,
//    val shouldTriggerCapture: Boolean = false,
//    val prescriptionFormCaptureBitmap: Bitmap? = null,
//)


package com.optictoolcompk.opticaltool.ui.billcreation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.datastore.BillDisplaySettingsManager
import com.optictoolcompk.opticaltool.data.models.*
import com.optictoolcompk.opticaltool.data.repository.BillRepository
import com.optictoolcompk.opticaltool.data.repository.PrescriptionRepository
import com.optictoolcompk.opticaltool.utils.BillImageStorage
import com.optictoolcompk.opticaltool.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BillCreationViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val billDisplaySettingsManager: BillDisplaySettingsManager, // ✅ Injected
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val billId: Long? = savedStateHandle.get<Long>("billId")

    private val _uiState = MutableStateFlow(BillCreationUiState(initialLoading = true))
    val uiState: StateFlow<BillCreationUiState> = _uiState.asStateFlow()

    private val _shopSettings = MutableStateFlow(ShopSettings())
    val shopSettings: StateFlow<ShopSettings> = _shopSettings.asStateFlow()

    // ✅ Now reading from DataStore
    val displaySettings: StateFlow<BillDisplaySettings> =
        billDisplaySettingsManager.billDisplaySettingsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BillDisplaySettings()
            )

    // Unpaid bills search
    private val _showUnpaidBillsDialog = MutableStateFlow(false)
    val showUnpaidBillsDialog: StateFlow<Boolean> = _showUnpaidBillsDialog.asStateFlow()

    private val _unpaidBillsSearchQuery = MutableStateFlow("")
    val unpaidBillsSearchQuery: StateFlow<String> = _unpaidBillsSearchQuery.asStateFlow()

    val unpaidBillsSearchResults: StateFlow<List<Bill>> =
        combine(
            _unpaidBillsSearchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.length >= 2) {
                        billRepository.searchUnpaidBills(query)
                    } else {
                        flowOf(emptyList())
                    }
                },
            uiState
        ) { bills, state ->
            bills.filterNot { it.id in state.selectedUnpaidBills }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    // Prescription search
    private val _showPrescriptionSearchDialog = MutableStateFlow(false)
    val showPrescriptionSearchDialog: StateFlow<Boolean> =
        _showPrescriptionSearchDialog.asStateFlow()

    private val _prescriptionSearchQuery = MutableStateFlow("")
    val prescriptionSearchQuery: StateFlow<String> = _prescriptionSearchQuery.asStateFlow()

    val prescriptionSearchResults: StateFlow<List<PrescriptionEntity>> =
        combine(
            _prescriptionSearchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.length >= 2) {
                        prescriptionRepository.searchPrescriptions(query)
                    } else {
                        flowOf(emptyList())
                    }
                },
            uiState
        ) { prescriptions, state ->
            prescriptions.filterNot { it.prescriptionImagePath in state.prescriptionImagesPaths }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            // Load shop settings first
            val settings = withContext(Dispatchers.IO) {
                billRepository.getShopSettings()
            }
            _shopSettings.value = settings

            if (billId != null && billId > 0) {
                loadBillForEdit(billId)
            } else {
                generateInvoiceNumber()
                _uiState.update { it.copy(initialLoading = false) }
            }
        }
    }

    private fun generateInvoiceNumber() {
        viewModelScope.launch {
            val nextNumber = billRepository.getNextInvoiceNumber()
            _uiState.update { it.copy(invoiceNumber = nextNumber) }
        }
    }

    suspend fun loadBillForEdit(id: Long) {
        try {
            val bill = withContext(Dispatchers.IO) {
                billRepository.getBillById(id)
            }

            if (bill != null) {
                _uiState.update {
                    it.copy(
                        initialLoading = false,
                        isEditMode = true,
                        invoiceNumber = bill.invoiceNumber,
                        invoiceDate = bill.invoiceDate,
                        invoiceTime = bill.invoiceTime,
                        customerName = bill.customerName,
                        customerPhone = bill.customerPhone,
                        customerCity = bill.customerCity,
                        items = bill.items,
                        totalAmount = bill.totalAmount,
                        discount = bill.discount.toString(),
                        advance = bill.advance.toString(),
                        advance2 = bill.advance2.toString(),
                        advance2Date = bill.advance2Date,
                        advance3 = bill.advance3.toString(),
                        advance3Date = bill.advance3Date,
                        previousAmount = bill.previousAmount,
                        remainingAmount = bill.remainingAmount,
                        remainingNote = bill.remainingNote,
                        pickupDate = bill.pickupDate,
                        imagePaths = bill.imagesPaths,
                        prescription = bill.prescription,
                        prescriptionImagesPaths = bill.prescriptionImagesPaths,
                        prescriptionFormData = bill.prescriptionFormData,
                        showPrescriptionFormCard = bill.prescriptionFormData != null && bill.prescriptionFormData.patientName.isNotBlank(),
                        prescriptionFormInputs = bill.prescriptionFormData ?: PrescriptionFormDataForBill()
                    )
                }
            } else {
                _uiState.update { it.copy(initialLoading = false, error = "Bill not found") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(initialLoading = false, error = "Failed to load bill: ${e.message}") }
        }
    }

    // Customer info
    fun onCustomerNameChanged(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun onCustomerPhoneChanged(phone: String) {
        _uiState.update { it.copy(customerPhone = phone) }
    }

    fun onCustomerCityChanged(city: String) {
        _uiState.update { it.copy(customerCity = city) }
    }

    // Invoice date/time
    fun onInvoiceDateChanged(date: String) {
        _uiState.update { it.copy(invoiceDate = date) }
    }

    fun onInvoiceTimeChanged(time: String) {
        _uiState.update { it.copy(invoiceTime = time) }
    }

    // Items management
    fun onAddItem() {
        val currentItems = _uiState.value.items.toMutableList()
        currentItems.add(BillItem())
        _uiState.update { it.copy(items = currentItems) }
    }

    fun onRemoveItem(index: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _uiState.update { it.copy(items = currentItems) }
            calculateTotals()
        }
    }

    fun onItemChanged(index: Int, item: BillItem) {
        val currentItems = _uiState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = item.copy(
                quantity = item.quantity,
                price = item.price
            )
            _uiState.update { it.copy(items = currentItems) }
            calculateTotals()
        }
    }

    // ==================== PRESCRIPTION MANAGEMENT ====================

    fun onShowPrescriptionOptionsDialog() {
        if (_uiState.value.prescriptionFormData != null || _uiState.value.showPrescriptionFormCard) {
            _uiState.update { it.copy(error = "You can only add one new prescription per bill") }
            return
        }
        _uiState.update { it.copy(showPrescriptionOptionsDialog = true) }
    }

    fun onHidePrescriptionOptionsDialog() {
        _uiState.update { it.copy(showPrescriptionOptionsDialog = false) }
    }

    fun onAddSavedPrescriptionImage(imagePath: String) {
        val currentImages = _uiState.value.prescriptionImagesPaths.toMutableList()
        if (currentImages.size < 3) {
            currentImages.add(imagePath)
            _uiState.update { it.copy(prescriptionImagesPaths = currentImages) }
            onHidePrescriptionSearchDialog()
        }
    }

    fun onRemovePrescriptionImage(index: Int) {
        val currentImages = _uiState.value.prescriptionImagesPaths.toMutableList()
        if (index in currentImages.indices) {
            val removedPath = currentImages.removeAt(index)
            _uiState.update {
                it.copy(
                    prescriptionImagesPaths = currentImages,
                    newPrescriptionImagesToAutoSave = it.newPrescriptionImagesToAutoSave - removedPath
                )
            }
        }
    }

    fun onPrescriptionImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isUploadingImages = true) }

                val result = BillImageStorage.saveImage(context, uri)
                if (result.isSuccess) {
                    val imagePath = result.getOrThrow()
                    val nextNo = getNextPrescriptionNumber()

                    val formData = PrescriptionFormDataForBill(
                        prescriptionNumber = nextNo,
                        prescriptionImagePath = imagePath
                    )

                    _uiState.update {
                        it.copy(
                            prescriptionFormData = formData,
                            newPrescriptionImagesToAutoSave = it.newPrescriptionImagesToAutoSave + imagePath,
                            isUploadingImages = false
                        )
                    }
                }

                onHidePrescriptionOptionsDialog()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingImages = false,
                        error = "Failed to add prescription image: ${e.message}"
                    )
                }
            }
        }
    }

    fun onShowPrescriptionForm() {
        if (_uiState.value.prescriptionFormData != null) {
            _uiState.update { it.copy(error = "You can only add one new prescription per bill") }
            return
        }
        viewModelScope.launch {
            val nextNo = getNextPrescriptionNumber()
            _uiState.update {
                it.copy(
                    showPrescriptionFormCard = true,
                    showPrescriptionOptionsDialog = false,
                    prescriptionFormInputs = it.prescriptionFormInputs.copy(prescriptionNumber = nextNo)
                )
            }
        }
    }

    fun onHidePrescriptionForm() {
        _uiState.update { it.copy(showPrescriptionFormCard = false) }
    }

    fun onPrescriptionFormInputChanged(inputs: PrescriptionFormDataForBill) {
        _uiState.update { it.copy(prescriptionFormInputs = inputs) }
    }

    fun onSavePrescriptionForm(
        context: Context,
        prescriptionBitmap: Bitmap
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCapturingPrescriptionForm = true) }

                val formData = _uiState.value.prescriptionFormInputs
                val prescriptionNumber = formData.prescriptionNumber

                val imagePath = BillImageStorage.savePrescriptionImage(
                    context,
                    prescriptionBitmap,
                    prescriptionNumber
                )

                val updatedFormData = formData.copy(
                    prescriptionImagePath = imagePath
                )

                _uiState.update {
                    it.copy(
                        prescriptionFormData = updatedFormData,
                        isCapturingPrescriptionForm = false,
                        prescriptionFormCaptureBitmap = prescriptionBitmap
                    )
                }

                performSaveBill()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCapturingPrescriptionForm = false,
                        error = "Failed to save prescription form: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun savePrescriptionToDatabase(
        prescriptionNumber: String,
        formData: PrescriptionFormDataForBill,
        bitmap: Bitmap? = null,
        imagePath: String? = null
    ) {
        try {
            if (formData.patientName.isNotBlank() && bitmap != null) {
                prescriptionRepository.createPrescription(
                    prescriptionNumber = prescriptionNumber,
                    patientName = formData.patientName,
                    phone = formData.phone,
                    age = formData.age,
                    city = formData.city,
                    rightSph = formData.rightSph,
                    rightCyl = formData.rightCyl,
                    rightAxis = formData.rightAxis,
                    rightVa = formData.rightVa,
                    leftSph = formData.leftSph,
                    leftCyl = formData.leftCyl,
                    leftAxis = formData.leftAxis,
                    leftVa = formData.leftVa,
                    add = formData.addPower,
                    ipdN = formData.ipdNear,
                    ipdD = formData.ipdDistance,
                    checkedBy = formData.checkedBy,
                    prescriptionBitmap = bitmap
                )
            } else if (imagePath != null) {
                prescriptionRepository.createImageOnlyPrescription(
                    prescriptionNumber = prescriptionNumber,
                    imageFile = java.io.File(imagePath)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClearPrescriptionForm() {
        _uiState.update {
            it.copy(
                prescriptionFormData = null,
                showPrescriptionFormCard = false,
                prescriptionFormInputs = PrescriptionFormDataForBill(),
                newPrescriptionImagesToAutoSave = emptySet(),
                prescriptionFormCaptureBitmap = null
            )
        }
    }

    suspend fun getNextPrescriptionNumber(): String {
        return prescriptionRepository.getNextPrescriptionNumber()
    }

    // Amounts
    fun onDiscountChanged(discount: String) {
        _uiState.update { it.copy(discount = discount) }
        calculateTotals()
    }

    fun onAdvanceChanged(advance: String) {
        _uiState.update { it.copy(advance = advance) }
        calculateTotals()
    }

    fun onAdvance2Changed(advance2: String) {
        _uiState.update {
            it.copy(
                advance2 = advance2,
                advance2Date = if (advance2.isNotBlank() && it.advance2Date == null) {
                    DateTimeUtils.getCurrentDate()
                } else if (advance2.isBlank()) {
                    null
                } else {
                    it.advance2Date
                }
            )
        }
        calculateTotals()
    }

    fun onAdvance2DateChanged(date: String?) {
        _uiState.update { it.copy(advance2Date = date) }
    }

    fun onAdvance3Changed(advance3: String) {
        _uiState.update {
            it.copy(
                advance3 = advance3,
                advance3Date = if (advance3.isNotBlank() && it.advance3Date == null) {
                    DateTimeUtils.getCurrentDate()
                } else if (advance3.isBlank()) {
                    null
                } else {
                    it.advance3Date
                }
            )
        }
        calculateTotals()
    }

    fun onAdvance3DateChanged(date: String?) {
        _uiState.update { it.copy(advance3Date = date) }
    }

    private fun calculateTotals() {
        val total = _uiState.value.items.sumOf { it.total }
        val discount = _uiState.value.discount.toDoubleOrNull() ?: 0.0
        val advance = _uiState.value.advance.toDoubleOrNull() ?: 0.0
        val advance2 = _uiState.value.advance2.toDoubleOrNull() ?: 0.0
        val advance3 = _uiState.value.advance3.toDoubleOrNull() ?: 0.0
        val previous = _uiState.value.previousAmount

        val remaining = total - discount - advance - advance2 - advance3 + previous

        _uiState.update {
            it.copy(
                totalAmount = total,
                remainingAmount = remaining
            )
        }
    }

    fun onPickupDateChanged(date: String?) {
        _uiState.update { it.copy(pickupDate = date) }
    }

    // Images
    suspend fun onImagesSelected(context: Context, uris: List<Uri>) {
        val currentImages = _uiState.value.imagePaths.toMutableList()
        if (currentImages.size >= 4) return

        _uiState.update { it.copy(isUploadingImages = true) }

        try {
            val remainingSlots = 4 - currentImages.size
            val urisToProcess = uris.take(remainingSlots)

            val result = BillImageStorage.saveImages(context, urisToProcess)
            if (result.isSuccess) {
                currentImages.addAll(result.getOrThrow())
                _uiState.update {
                    it.copy(
                        imagePaths = currentImages,
                        isUploadingImages = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isUploadingImages = false,
                        error = "Failed to save images"
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isUploadingImages = false,
                    error = "Failed to upload images: ${e.message}"
                )
            }
        }
    }

    fun onImageRemoved(imagePath: String) {
        val currentImages = _uiState.value.imagePaths.toMutableList()
        currentImages.remove(imagePath)
        _uiState.update { it.copy(imagePaths = currentImages) }

        viewModelScope.launch {
            BillImageStorage.deleteImage(imagePath)
        }
    }

    // ✅ Display settings - Now persists to DataStore
    fun onDisplaySettingsChanged(settings: BillDisplaySettings) {
        viewModelScope.launch {
            billDisplaySettingsManager.updateAllSettings(settings)
        }
    }

    // Unpaid bills search
    fun onShowUnpaidBillsDialog() {
        _showUnpaidBillsDialog.value = true
        _unpaidBillsSearchQuery.value = ""
    }

    fun onHideUnpaidBillsDialog() {
        _showUnpaidBillsDialog.value = false
        _unpaidBillsSearchQuery.value = ""
    }

    fun onUnpaidBillsSearchQueryChanged(query: String) {
        _unpaidBillsSearchQuery.value = query
    }

    fun onUnpaidBillSelected(bill: Bill) {
        _uiState.update {
            it.copy(
                previousAmount = it.previousAmount + bill.remainingAmount,
                selectedUnpaidBills = it.selectedUnpaidBills + bill.id,
                selectedInvoiceNumbers = it.selectedInvoiceNumbers + bill.invoiceNumber
            )
        }
        calculateTotals()
        onHideUnpaidBillsDialog()
    }

    private fun buildCurrentBillRemainingNote(): String? {
        val invoices = _uiState.value.selectedInvoiceNumbers
        if (invoices.isEmpty()) return _uiState.value.remainingNote

        return "Includes unpaid invoices: ${invoices.joinToString(", ")}"
    }

    // Prescription search
    fun onShowPrescriptionSearchDialog() {
        _showPrescriptionSearchDialog.value = true
        _prescriptionSearchQuery.value = ""
    }

    fun onHidePrescriptionSearchDialog() {
        _showPrescriptionSearchDialog.value = false
        _prescriptionSearchQuery.value = ""
    }

    fun onPrescriptionSearchQueryChanged(query: String) {
        _prescriptionSearchQuery.value = query
    }

    fun saveBill() {
        viewModelScope.launch {
            try {
                if (_uiState.value.customerName.isBlank()) {
                    _uiState.update { it.copy(error = "Customer name is required") }
                    return@launch
                }

                if (_uiState.value.customerPhone.isBlank()) {
                    _uiState.update { it.copy(error = "Customer phone is required") }
                    return@launch
                }

                val validItems = _uiState.value.items.filter {
                    it.itemName.isNotBlank() && it.quantity > 0 && it.price > 0
                }

                if (validItems.isEmpty()) {
                    _uiState.update { it.copy(error = "At least one valid item is required") }
                    return@launch
                }

                if (_uiState.value.showPrescriptionFormCard) {
                    if (_uiState.value.prescriptionFormInputs.patientName.isBlank()) {
                        _uiState.update { it.copy(error = "Patient name on prescription is required") }
                        return@launch
                    }
                }

                if (_uiState.value.showPrescriptionFormCard) {
                    _uiState.update { it.copy(shouldTriggerCapture = true) }
                } else {
                    performSaveBill()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(error = "Error: ${e.message}") }
            }
        }
    }

    private suspend fun performSaveBill() {
        _uiState.update { it.copy(isSaving = true, shouldTriggerCapture = false) }

        val state = _uiState.value
        val currentDisplaySettings = displaySettings.value

        if (currentDisplaySettings.autoSavePrescriptions && state.prescriptionFormData != null) {
            val formData = state.prescriptionFormData

            if (state.newPrescriptionImagesToAutoSave.contains(formData.prescriptionImagePath)) {
                savePrescriptionToDatabase(
                    prescriptionNumber = formData.prescriptionNumber,
                    formData = formData,
                    imagePath = formData.prescriptionImagePath
                )
            }
            else if (formData.patientName.isNotBlank() && state.prescriptionFormCaptureBitmap != null) {
                savePrescriptionToDatabase(
                    prescriptionNumber = formData.prescriptionNumber,
                    formData = formData,
                    bitmap = state.prescriptionFormCaptureBitmap
                )
            }
        }

        val bill = Bill(
            id = billId ?: 0,
            invoiceNumber = state.invoiceNumber,
            invoiceDate = state.invoiceDate,
            invoiceTime = state.invoiceTime,
            customerName = state.customerName,
            customerPhone = state.customerPhone,
            customerCity = state.customerCity,
            items = state.items.filter { it.itemName.isNotBlank() },
            totalAmount = state.totalAmount,
            discount = state.discount.toDoubleOrNull() ?: 0.0,
            advance = state.advance.toDoubleOrNull() ?: 0.0,
            advance2 = state.advance2.toDoubleOrNull() ?: 0.0,
            advance2Date = state.advance2Date,
            advance3 = state.advance3.toDoubleOrNull() ?: 0.0,
            advance3Date = state.advance3Date,
            previousAmount = state.previousAmount,
            remainingAmount = state.remainingAmount,
            remainingNote = buildCurrentBillRemainingNote(),
            pickupDate = state.pickupDate,
            prescriptionImagesPaths = state.prescriptionImagesPaths,
            prescriptionFormData = state.prescriptionFormData,
            imagesPaths = state.imagePaths
        )

        val result = if (state.isEditMode && billId != null) {
            billRepository.updateBill(bill)
        } else {
            billRepository.createBill(bill)
        }

        if (result.isSuccess) {
            if (state.selectedUnpaidBills.isNotEmpty()) {
                billRepository.settleBills(
                    billIds = state.selectedUnpaidBills,
                    newInvoiceNumber = state.invoiceNumber
                )
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = true,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    error = "Failed to save bill"
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

data class BillCreationUiState(
    val initialLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val invoiceNumber: String = "",
    val invoiceDate: String = DateTimeUtils.getCurrentDate(),
    val invoiceTime: String = DateTimeUtils.getCurrentTime(),
    val customerName: String = "",
    val customerPhone: String = "",
    val customerCity: String = "",
    val items: List<BillItem> = listOf(BillItem()),
    val totalAmount: Double = 0.0,
    val discount: String = "",
    val advance: String = "",
    val advance2: String = "",
    val advance2Date: String? = null,
    val advance3: String = "",
    val advance3Date: String? = null,
    val previousAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val remainingNote: String? = null,
    val selectedUnpaidBills: Set<Long> = emptySet(),
    val selectedInvoiceNumbers: List<String> = emptyList(),
    val pickupDate: String? = null,
    val imagePaths: List<String> = emptyList(),
    val prescription: PrescriptionData? = null,
    val isSaving: Boolean = false,
    val isUploadingImages: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val prescriptionImagesPaths: List<String> = emptyList(),
    val newPrescriptionImagesToAutoSave: Set<String> = emptySet(),
    val prescriptionFormData: PrescriptionFormDataForBill? = null,
    val prescriptionFormInputs: PrescriptionFormDataForBill = PrescriptionFormDataForBill(),
    val showPrescriptionOptionsDialog: Boolean = false,
    val showPrescriptionFormCard: Boolean = false,
    val isCapturingPrescriptionForm: Boolean = false,
    val shouldTriggerCapture: Boolean = false,
    val prescriptionFormCaptureBitmap: Bitmap? = null,
)
