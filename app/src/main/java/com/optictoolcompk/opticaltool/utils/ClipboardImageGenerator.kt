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

    private const val PAGE_WIDTH = 600f
    private const val MARGIN = 30f
    private const val ROW_HEIGHT = 50f
    private const val HEADER_HEIGHT = 80f

    private const val COL_SR_X = 60f
    private const val COL_QUALITY_X = 180f
    private const val COL_NUMBER_X = 390f
    private const val COL_PAIRS_X = 550f

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


            canvas.drawText(
                "Clipboard Order - Page $pageNumber of $totalPages",
                PAGE_WIDTH / 2,
                y + 35f,
                paints.titlePaint
            )
            y += HEADER_HEIGHT


            val headerRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT)
            canvas.drawRect(headerRect, paints.headerBgPaint)


            canvas.drawText("Sr.", COL_SR_X, y + 32f, paints.headerTextPaint)
            canvas.drawText("Quality", COL_QUALITY_X, y + 32f, paints.headerTextPaint)
            canvas.drawText("Number", COL_NUMBER_X, y + 32f, paints.headerTextPaint)
            canvas.drawText("Pairs", COL_PAIRS_X, y + 32f, paints.headerTextPaintRight)

            y += ROW_HEIGHT


            rows.forEachIndexed { index, row ->
                val rowRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_HEIGHT)

                // Alternate row background for better readability
                if (index % 2 == 0) {
                    canvas.drawRect(rowRect, paints.rowAltBgPaint)
                }


                canvas.drawText(
                    row.globalIndex.toString(),
                    COL_SR_X,
                    y + 33f,
                    paints.rowTextPaintBold
                )

                canvas.drawText(
                    row.sectionName,
                    COL_QUALITY_X,
                    y + 33f,
                    paints.rowTextPaintBold
                )

                canvas.drawText(
                    row.getFormattedNumber(),
                    COL_NUMBER_X,
                    y + 33f,
                    paints.rowTextPaintBold
                )

                canvas.drawText(
                    row.pairs.toString(),
                    COL_PAIRS_X,
                    y + 33f,
                    paints.rowTextPaintBoldRight
                )


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
            textSize = 34f  // Slightly larger
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }


        val headerBgPaint = Paint().apply {
            color = "#2C3E50".toColorInt()  // Professional dark blue-gray
            style = Paint.Style.FILL
        }


        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f  // Increased from 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val headerTextPaintRight = Paint().apply {
            color = Color.WHITE
            textSize = 20f  // Increased from 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }


        val rowTextPaintBold = Paint().apply {
            color = "#1A1A1A".toColorInt()  // Slightly softer black
            textSize = 20f  // Increased from 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val rowTextPaintBoldRight = Paint().apply {
            color = "#1A1A1A".toColorInt()
            textSize = 20f  // Increased from 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }


        val rowAltBgPaint = Paint().apply {
            color = "#F8F9FA".toColorInt()  // Very light gray
            style = Paint.Style.FILL
        }


        val dividerPaint = Paint().apply {
            color = "#E0E0E0".toColorInt()  // Light gray
            strokeWidth = 1.5f  // Slightly thicker
        }
    }
}
