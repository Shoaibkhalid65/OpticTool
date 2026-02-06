package com.optictoolcompk.opticaltool.ui.prescriptioncreation


import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.models.PrescriptionFormData
import com.optictoolcompk.opticaltool.data.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val repository: PrescriptionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    // Get prescription ID from navigation arguments (for edit mode)
    private val prescriptionId: Long? = savedStateHandle.get<Long>("prescriptionId")

    private val _prescriptionNumber = MutableStateFlow("")
    val prescriptionNumber: StateFlow<String> = _prescriptionNumber.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()


    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // Form data states for edit mode
    private val _formData = MutableStateFlow<PrescriptionFormData?>(null)
    val formData: StateFlow<PrescriptionFormData?> = _formData.asStateFlow()

    init {
        if (prescriptionId != null) {
            // Edit mode
            _isEditMode.value = true
            loadPrescriptionForEdit(prescriptionId)
        } else {
            // Create mode
            _isEditMode.value = false
            generateNewPrescriptionNumber()
            _currentDate.value = getCurrentDate()
        }
    }

    private fun loadPrescriptionForEdit(id: Long) {
        viewModelScope.launch {
            try {
                val prescription = repository.getPrescriptionById(id)
                if (prescription != null) {
                    _prescriptionNumber.value = prescription.prescriptionNumber
                    _currentDate.value = formatTimestampToDate(prescription.createdAt)
                    _formData.value = PrescriptionFormData(
                        patientName = prescription.patientName,
                        phone = prescription.phone,
                        age = prescription.age,
                        city = prescription.city,
                        rightSph = prescription.rightSph,
                        rightCyl = prescription.rightCyl,
                        rightAxis = prescription.rightAxis,
                        rightVa = prescription.rightVa,
                        leftSph = prescription.leftSph,
                        leftCyl = prescription.leftCyl,
                        leftAxis = prescription.leftAxis,
                        leftVa = prescription.leftVa,
                        addPower = prescription.addPower,
                        ipdNear = prescription.ipdNear,
                        ipdDistance = prescription.ipdDistance,
                        checkedBy = prescription.checkedBy
                    )
                }
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Failed to load prescription")
            }
        }
    }

    fun generateNewPrescriptionNumber() {
        viewModelScope.launch {
            try {
                val nextNumber = repository.getNextPrescriptionNumber()
                _prescriptionNumber.value = nextNumber
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Failed to generate prescription number")
            }
        }
    }


    fun savePrescription(
        patientName: String,
        phone: String,
        age: String,
        city: String,
        rightSph: String,
        rightCyl: String,
        rightAxis: String,
        rightVa: String,
        leftSph: String,
        leftCyl: String,
        leftAxis: String,
        leftVa: String,
        add: String,
        ipdN: String,
        ipdD: String,
        checkedBy: String,
        image: Bitmap?=null
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            val result = if (_isEditMode.value && prescriptionId != null) {
                // Update existing prescription
                updateExistingPrescription(
                    prescriptionId,
                    patientName, phone, age, city,
                    rightSph, rightCyl, rightAxis, rightVa,
                    leftSph, leftCyl, leftAxis, leftVa,
                    add, ipdN, ipdD, checkedBy,image
                )
            } else {
                // Create new prescription
                repository.createPrescription(
                    prescriptionNumber = _prescriptionNumber.value,
                    patientName = patientName,
                    phone = phone,
                    age = age,
                    city = city,
                    rightSph = rightSph,
                    rightCyl = rightCyl,
                    rightAxis = rightAxis,
                    rightVa = rightVa,
                    leftSph = leftSph,
                    leftCyl = leftCyl,
                    leftAxis = leftAxis,
                    leftVa = leftVa,
                    add = add,
                    ipdN = ipdN,
                    ipdD = ipdD,
                    checkedBy = checkedBy,
                    prescriptionBitmap = image
                )
            }

            _saveState.value = if (result.isSuccess) {
                SaveState.Success
            } else {
                SaveState.Error(result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    private suspend fun updateExistingPrescription(
        id: Long,
        patientName: String,
        phone: String,
        age: String,
        city: String,
        rightSph: String,
        rightCyl: String,
        rightAxis: String,
        rightVa: String,
        leftSph: String,
        leftCyl: String,
        leftAxis: String,
        leftVa: String,
        add: String,
        ipdN: String,
        ipdD: String,
        checkedBy: String,
        image: Bitmap?
    ): Result<Unit> {
        val existingPrescription = repository.getPrescriptionById(id) ?:
        return Result.failure(Exception("Prescription not found"))

        // Keep the old image path if no new bitmap is captured
        val imagePath = if (image != null) {
            // Delete old image if it exists
            repository.deletePrescriptionImage(existingPrescription.prescriptionImagePath)
            // Save new image
            repository.savePrescriptionImageFromBitmap(
                image,
                existingPrescription.prescriptionNumber
            )
        } else {
            existingPrescription.prescriptionImagePath
        }

        val updatedPrescription = existingPrescription.copy(
            createdAt = System.currentTimeMillis(),
            patientName = patientName,
            phone = phone,
            age = age,
            city = city,
            rightSph = rightSph,
            rightCyl = rightCyl,
            rightAxis = rightAxis,
            rightVa = rightVa,
            leftSph = leftSph,
            leftCyl = leftCyl,
            leftAxis = leftAxis,
            leftVa = leftVa,
            addPower = add,
            ipdNear = ipdN,
            ipdDistance = ipdD,
            checkedBy = checkedBy,
            prescriptionImagePath = imagePath
        )

        return repository.updatePrescription(updatedPrescription)
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // In PrescriptionCreationViewModel.kt

    fun saveImageOnlyPrescription(imageFile: File) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            // Generate new prescription number
            val prescriptionNumber = repository.getNextPrescriptionNumber()

            val result = repository.createImageOnlyPrescription(
                prescriptionNumber = prescriptionNumber,
                imageFile = imageFile
            )

            _saveState.value = if (result.isSuccess) {
                SaveState.Success
            } else {
                SaveState.Error(result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }
}





sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}