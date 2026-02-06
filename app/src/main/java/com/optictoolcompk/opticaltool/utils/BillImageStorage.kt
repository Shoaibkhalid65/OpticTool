package com.optictoolcompk.opticaltool.utils


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object BillImageStorage {

    private const val BILLS_IMAGES_DIR = "bill_images"
    private const val MAX_IMAGES_PER_BILL = 4

    /**
     * Get bills images directory
     */
    private fun getBillImagesDir(context: Context): File {
        val dir = File(context.filesDir, BILLS_IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Save image from URI to internal storage
     * Returns the local file path
     */
    suspend fun saveImage(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Compress image first
            val compressedFile = ImageCompressionUtil.compressImage(
                context = context,
                uri = imageUri,
                targetSizeKB = 250L
            ) ?: return@withContext Result.failure(Exception("Failed to compress image"))

            // Generate unique filename
            val fileName = "bill_${UUID.randomUUID()}.jpg"
            val destFile = File(getBillImagesDir(context), fileName)

            // Copy compressed file to bills directory
            compressedFile.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Delete compressed temp file
            compressedFile.delete()

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Save multiple images (up to 4)
     */
    suspend fun saveImages(
        context: Context,
        imageUris: List<Uri>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val savedPaths = mutableListOf<String>()

            // Take only first 4 images
            val urisToSave = imageUris.take(MAX_IMAGES_PER_BILL)

            urisToSave.forEach { uri ->
                val result = saveImage(context, uri)
                if (result.isSuccess) {
                    savedPaths.add(result.getOrThrow())
                }
            }

            Result.success(savedPaths)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Delete image from internal storage
     */
    suspend fun deleteImage(imagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Delete multiple images
     */
    suspend fun deleteImages(imagePaths: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            imagePaths.forEach { path ->
                deleteImage(path)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get File object from path
     */
    fun getImageFile(imagePath: String): File? {
        return try {
            val file = File(imagePath)
            if (file.exists()) file else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get Uri from file path
     */
    fun getImageUri(context: Context, imagePath: String): Uri? {
        return try {
            val file = getImageFile(imagePath)
            file?.let { Uri.fromFile(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if image file exists
     */
    fun imageExists(imagePath: String): Boolean {
        return try {
            File(imagePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get image size in KB
     */
    fun getImageSizeKB(imagePath: String): Long {
        return try {
            val file = File(imagePath)
            if (file.exists()) file.length() / 1024 else 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Clean up orphaned images (images not referenced by any bill)
     * Call this periodically to free up space
     */
    suspend fun cleanupOrphanedImages(
        context: Context,
        referencedPaths: Set<String>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val billImagesDir = getBillImagesDir(context)
            val allImages = billImagesDir.listFiles() ?: emptyArray()

            var deletedCount = 0

            allImages.forEach { file ->
                if (!referencedPaths.contains(file.absolutePath)) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }

            Result.success(deletedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun savePrescriptionImage(
        context: Context,
        bitmap: Bitmap,
        prescriptionNumber: String
    ): String {
        val directory = File(context.filesDir, "bill_prescriptions")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val filename = "${prescriptionNumber}_${System.currentTimeMillis()}.jpg"
        val file = File(directory, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return file.absolutePath
    }
}