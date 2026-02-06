package com.optictoolcompk.opticaltool.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object ReceiptSharingHelper {

    /**
     * Share receipt image via WhatsApp or other apps
     */
    suspend fun shareReceiptImage(
        context: Context,
        imageUri: Uri,
        billNumber: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, "Bill #$billNumber")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Bill via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooserIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Share receipt specifically via WhatsApp
     */
    suspend fun shareViaWhatsApp(
        context: Context,
        imageUri: Uri,
        billNumber: String,
        phoneNumber: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, "Bill #$billNumber")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // If phone number provided, try to open specific chat
                phoneNumber?.let {
                    val cleanNumber = it.replace("+", "").replace(" ", "")
                    putExtra("jid", "$cleanNumber@s.whatsapp.net")
                }
            }

            whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(whatsappIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            // WhatsApp not installed or other error
            Toast.makeText(
                context,
                "WhatsApp not installed. Using default share.",
                Toast.LENGTH_SHORT
            ).show()
            shareReceiptImage(context, imageUri, billNumber)
        }
    }


    /**
     * Save receipt image to gallery
     */
    suspend fun saveToGallery(
        context: Context,
        imageUri: Uri,
        billNumber: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "Bill_${billNumber}_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Bills")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(IOException("Failed to create MediaStore entry"))

                resolver.openOutputStream(uri)?.use { outputStream ->
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Result.success(uri)
            } else {
                // Legacy approach for older Android versions
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val billsDir = File(picturesDir, "Bills")
                if (!billsDir.exists()) {
                    billsDir.mkdirs()
                }

                val destFile = File(billsDir, fileName)

                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }


                Result.success(Uri.fromFile(destFile))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Share receipt PDF
     */
    suspend fun shareReceiptPdf(
        context: Context,
        pdfUri: Uri,
        billNumber: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_SUBJECT, "Bill #$billNumber")
                putExtra(Intent.EXTRA_TEXT, "Please find attached bill #$billNumber")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share PDF via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooserIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

}

