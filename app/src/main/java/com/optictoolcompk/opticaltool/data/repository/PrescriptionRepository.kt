package com.optictoolcompk.opticaltool.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.optictoolcompk.opticaltool.data.local.dao.PrescriptionDao
import com.optictoolcompk.opticaltool.data.models.PrescriptionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrescriptionRepository @Inject constructor(
    private val prescriptionDao: PrescriptionDao,
    @param:ApplicationContext private val context: Context
) {

    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getAllPrescriptions()
    }

    fun searchPrescriptions(query: String): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.searchPrescriptions(query)
    }

    suspend fun getPrescriptionById(id: Long): PrescriptionEntity? {
        return prescriptionDao.getPrescriptionById(id)
    }

    /**
     * Get the next prescription number WITHOUT saving to database
     */
    suspend fun getNextPrescriptionNumber(): String {
        val nextId = (prescriptionDao.getLastPrescriptionId() ?: 0) + 1
        return generatePrescriptionNumber(nextId)
    }

    /**
     * Create prescription with captured image
     */
    suspend fun createPrescription(
        prescriptionNumber: String,
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
        prescriptionBitmap: Bitmap?
    ): Result<Long> {
        return try {
            // Check if prescription number already exists
            val existing = prescriptionDao.getPrescriptionByNumber(prescriptionNumber)
            if (existing != null) {
                return Result.failure(Exception("Prescription number already exists"))
            }

            // Save image to internal storage
            val imagePath = prescriptionBitmap?.let {
                savePrescriptionImage(it, prescriptionNumber)
            } ?: ""

            val prescription = PrescriptionEntity(
                prescriptionNumber = prescriptionNumber,
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

            val id = prescriptionDao.insert(prescription)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create image-only prescription (for camera/gallery uploads)
     */
    suspend fun createImageOnlyPrescription(
        prescriptionNumber: String,
        imageFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Copy image to app's prescription images directory
            val imagePath = copyImageToAppStorage(imageFile, prescriptionNumber)

            val prescription = PrescriptionEntity(
                prescriptionNumber = prescriptionNumber,
                createdAt = System.currentTimeMillis(),
                patientName = "", // Empty for image-only prescriptions
                phone = "",
                age = "",
                city = "",
                rightSph = "",
                rightCyl = "",
                rightAxis = "",
                rightVa = "",
                leftSph = "",
                leftCyl = "",
                leftAxis = "",
                leftVa = "",
                addPower = "",
                ipdNear = "",
                ipdDistance = "",
                checkedBy = "",
                prescriptionImagePath = imagePath
            )

            prescriptionDao.insert(prescription)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update existing prescription
     */
    suspend fun updatePrescription(prescription: PrescriptionEntity): Result<Unit> {
        return try {
            prescriptionDao.update(prescription)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete prescription and its image
     */
    suspend fun deletePrescription(prescription: PrescriptionEntity): Result<Unit> {
        return try {
            // Delete image file if exists
            deletePrescriptionImage(prescription.prescriptionImagePath)

            // Delete from database
            prescriptionDao.delete(prescription)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ======================== IMAGE STORAGE METHODS ========================

    /**
     * Get the directory where prescription images are stored
     * Uses the same directory as savePrescriptionImage for consistency
     */
    private fun getImageStorageDir(): File {
        val directory = File(context.filesDir, "prescriptions")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    /**
     * Save prescription image to internal storage
     */
    fun savePrescriptionImage(bitmap: Bitmap, prescriptionNumber: String): String {
        val directory = getImageStorageDir()

        val filename = "${prescriptionNumber}_${System.currentTimeMillis()}.jpg"
        val file = File(directory, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return file.absolutePath
    }

    /**
     * Save prescription image from bitmap (for edit mode)
     */
    fun savePrescriptionImageFromBitmap(bitmap: Bitmap, prescriptionNumber: String): String {
        return savePrescriptionImage(bitmap, prescriptionNumber)
    }

    /**
     * Copy image file to app's prescription storage
     * Used for camera/gallery uploads
     */
    private fun copyImageToAppStorage(sourceFile: File, prescriptionNumber: String): String {
        val fileName = "${prescriptionNumber}_${System.currentTimeMillis()}.jpg"
        val destFile = File(getImageStorageDir(), fileName)
        sourceFile.copyTo(destFile, overwrite = true)
        return destFile.absolutePath
    }

    /**
     * Delete prescription image from internal storage
     */
    fun deletePrescriptionImage(imagePath: String) {
        if (imagePath.isNotEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     * Get prescription image file
     */
    fun getPrescriptionImageFile(imagePath: String): File? {
        if (imagePath.isEmpty()) return null
        val file = File(imagePath)
        return if (file.exists()) file else null
    }

    private fun generatePrescriptionNumber(id: Long): String {
        return "RX${id.toString().padStart(3, '0')}"
    }
}