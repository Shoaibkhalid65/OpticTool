package com.optictoolcompk.opticaltool.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.optictoolcompk.opticaltool.data.models.ClipboardRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ClipboardImageGenerator {

    private const val PAGE_WIDTH = 800f
    private const val MARGIN = 40f
    private const val ROW_HEIGHT = 40f
    private const val HEADER_HEIGHT = 60f

    suspend fun generateAndSaveImage(
        context: Context,
        rows: List<ClipboardRow>,
        pageNumber: Int,
        totalPages: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentHeight = HEADER_HEIGHT + (rows.size * ROW_HEIGHT) + MARGIN * 2 + 40f
            val bitmap = createBitmap(PAGE_WIDTH.toInt(), contentHeight.toInt())
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val paints = GeneratorPaints()
            var y = MARGIN

            // Title
            canvas.drawText(
                "Clipboard Order - Page $pageNumber of $totalPages",
                PAGE_WIDTH / 2,
                y + 30f,
                paints.titlePaint
            )
            y += HEADER_HEIGHT

            // Table Header Background
            val headerRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT)
            canvas.drawRect(headerRect, paints.headerBgPaint)

            // Header Text
            val colSr = MARGIN + 20f
            val colQuality = MARGIN + 100f
            val colNumber = PAGE_WIDTH / 2f + 50f
            val colPairs = PAGE_WIDTH - MARGIN - 20f

            canvas.drawText("Sr.", colSr, y + 28f, paints.headerTextPaint)
            canvas.drawText("Quality", colQuality + 50f, y + 28f, paints.headerTextPaint)
            canvas.drawText("Number", colNumber + 50f, y + 28f, paints.headerTextPaint)
            canvas.drawText("Pairs", colPairs, y + 28f, paints.headerTextPaintRight)

            y += ROW_HEIGHT

            // Rows
            rows.forEachIndexed { index, row ->
                val rowRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT)
                if (index % 2 == 0) {
                    canvas.drawRect(rowRect, paints.rowAltBgPaint)
                }

                canvas.drawText(row.globalIndex.toString(), colSr, y + 28f, paints.rowTextPaint)
                canvas.drawText(
                    row.sectionName,
                    colQuality + 50f,
                    y + 28f,
                    paints.rowTextPaintCenter
                )
                canvas.drawText(
                    row.getFormattedNumber(),
                    colNumber + 50f,
                    y + 28f,
                    paints.rowTextPaintCenter
                )
                canvas.drawText(row.pairs.toString(), colPairs, y + 28f, paints.rowTextPaintRight)

                // Row Divider
                canvas.drawLine(
                    MARGIN,
                    y + ROW_HEIGHT,
                    PAGE_WIDTH - MARGIN,
                    y + ROW_HEIGHT,
                    paints.dividerPaint
                )

                y += ROW_HEIGHT
            }

            // Save to Gallery
            saveBitmapToGallery(context, bitmap, "Clipboard_Order_P$pageNumber")

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Screenshot saved to Gallery", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String) {
        val name = "${fileName}_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/GlassesNotebook")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
    }

    private class GeneratorPaints {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val headerBgPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerTextPaintRight = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val rowTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isAntiAlias = true
        }
        val rowTextPaintCenter = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val rowTextPaintRight = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val rowAltBgPaint = Paint().apply {
            color = "#F2F2F2".toColorInt()
            style = Paint.Style.FILL
        }
        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
    }
}
