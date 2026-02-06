package com.optictoolcompk.opticaltool.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.core.graphics.scale

object ImageCompressionUtil {

    private const val TARGET_SIZE_KB = 250L
    private const val TARGET_SIZE_BYTES = TARGET_SIZE_KB * 1024

    // Quality settings for prescription text clarity
    private const val INITIAL_QUALITY = 90 // Start high for text clarity
    private const val MIN_QUALITY = 70 // Don't go below this (text becomes blurry)
    private const val QUALITY_STEP = 5 // Reduce by 5% each iteration

    // Max dimension to prevent memory issues with very large images
    private const val MAX_DIMENSION = 2048

    /**
     * Compress image to ~250KB while maintaining text clarity
     * This is optimized for prescription images with text
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        targetSizeKB: Long = TARGET_SIZE_KB
    ): File? = withContext(Dispatchers.IO) {
        try {
            // Step 1: Decode with proper orientation
            val (bitmap, orientation) = decodeBitmapFromUri(context, uri)
                ?: return@withContext null

            // Step 2: Fix rotation based on EXIF
            val rotatedBitmap = rotateBitmap(bitmap, orientation)

            // Step 3: Resize if image is too large (prevent memory issues)
            val resizedBitmap = resizeIfNeeded(rotatedBitmap, MAX_DIMENSION)

            // Step 4: Compress to target size with quality control
            val compressedFile = compressToTargetSize(
                context,
                resizedBitmap,
                targetSizeKB * 1024
            )

            // Step 5: Cleanup
            if (bitmap != rotatedBitmap) bitmap.recycle()
            if (rotatedBitmap != resizedBitmap) rotatedBitmap.recycle()
            resizedBitmap.recycle()

            compressedFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decode bitmap from URI with EXIF orientation
     */
    private fun decodeBitmapFromUri(
        context: Context,
        uri: Uri
    ): Pair<Bitmap, Int>? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Get EXIF orientation first (before decoding)
            val orientation = getExifOrientation(context, uri)

            // Decode bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null) {
                Pair(bitmap, orientation)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get EXIF orientation from URI
     */
    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Rotate bitmap based on EXIF orientation
     */
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap // No rotation needed
        }

        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Resize bitmap if it exceeds max dimension
     * Maintains aspect ratio
     */
    private fun resizeIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Check if resize is needed
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        // Calculate new dimensions maintaining aspect ratio
        val ratio = minOf(
            maxDimension.toFloat() / width,
            maxDimension.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return bitmap.scale(newWidth, newHeight)
    }

    /**
     * Compress bitmap to target size with iterative quality reduction
     * Optimized for text clarity in prescription images
     */
    private fun compressToTargetSize(
        context: Context,
        bitmap: Bitmap,
        targetSizeBytes: Long
    ): File {
        val outputFile = File(
            context.cacheDir,
            "compressed_${System.currentTimeMillis()}.jpg"
        )

        var quality = INITIAL_QUALITY
        var outputBytes: ByteArray

        // Iteratively reduce quality until we hit target size
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputBytes = outputStream.toByteArray()
            outputStream.close()

            // If size is good or quality is at minimum, stop
            if (outputBytes.size <= targetSizeBytes || quality <= MIN_QUALITY) {
                break
            }

            // Reduce quality for next iteration
            quality -= QUALITY_STEP

        } while (outputBytes.size > targetSizeBytes && quality >= MIN_QUALITY)

        // Write final compressed image to file
        FileOutputStream(outputFile).use { fos ->
            fos.write(outputBytes)
        }

        return outputFile
    }

    /**
     * Get file size in KB
     */
    fun getFileSizeKB(file: File): Long {
        return file.length() / 1024
    }

    /**
     * Get detailed compression info for debugging
     */
    data class CompressionInfo(
        val originalSizeKB: Long,
        val compressedSizeKB: Long,
        val compressionRatio: Float,
        val finalQuality: Int
    )

    /**
     * Compress with detailed info (useful for debugging/logging)
     */
    suspend fun compressImageWithInfo(
        context: Context,
        uri: Uri,
        targetSizeKB: Long = TARGET_SIZE_KB
    ): Pair<File, CompressionInfo>? = withContext(Dispatchers.IO) {
        try {
            // Get original size
            val originalSize = context.contentResolver.openInputStream(uri)?.use {
                it.available().toLong()
            } ?: 0L

            val compressedFile = compressImage(context, uri, targetSizeKB)
                ?: return@withContext null

            val compressedSize = compressedFile.length()

            val info = CompressionInfo(
                originalSizeKB = originalSize / 1024,
                compressedSizeKB = compressedSize / 1024,
                compressionRatio = originalSize.toFloat() / compressedSize.toFloat(),
                finalQuality = INITIAL_QUALITY // You can track this if needed
            )

            Pair(compressedFile, info)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

