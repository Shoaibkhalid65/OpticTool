package com.optictoolcompk.opticaltool.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.optictoolcompk.opticaltool.data.models.Bill
import com.optictoolcompk.opticaltool.data.models.ShopSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object BillReceiptGenerator {

    private const val PAGE_WIDTH = 595f // A4 width
    private const val MARGIN = 30f
    private const val LINE_SPACING = 18f

    data class ReceiptResult(val pdfUri: Uri?, val imageUri: Uri?, val error: String? = null)

    suspend fun generateReceipt(
        context: Context,
        bill: Bill,
        shopSettings: ShopSettings
    ): ReceiptResult = withContext(Dispatchers.IO) {
        try {
            // 1. Calculate required height
            val contentHeight = calculateHeight(bill, shopSettings)
            val bitmap = createBitmap(PAGE_WIDTH.toInt(), contentHeight.toInt())
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val paints = ReceiptPaints()
            var y = MARGIN

            // 2. Header
            y = drawHeader(canvas, bill, shopSettings, y, paints)

            // 3. Customer & Items
            y = drawCustomerAndItems(canvas, bill, shopSettings, y, paints)

            // 4. Split Section (Prescription Image Left | Calculations Right)
            y = drawSplitSection(canvas, bill, shopSettings, y, paints)

            // 5. Gallery (Remaining Images)
            y = drawImageGallery(canvas, bill, y, paints)

            // 6. Terms & Footer
            drawFooter(canvas, shopSettings, contentHeight - MARGIN, paints)

            // Save Image (High Quality JPG)
            val imageFile = File(context.cacheDir, "Bill_${bill.invoiceNumber}_img.jpg")
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            }
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Save PDF
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                PAGE_WIDTH.toInt(),
                contentHeight.toInt(),
                1
            ).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val pdfFile = File(context.cacheDir, "Bill_${bill.invoiceNumber}.pdf")
            FileOutputStream(pdfFile).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            val pdfUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            bitmap.recycle()
            ReceiptResult(pdfUri, imageUri)
        } catch (e: Exception) {
            e.printStackTrace()
            ReceiptResult(null, null, e.message)
        }
    }

    private fun drawHeader(
        canvas: Canvas,
        bill: Bill,
        shop: ShopSettings,
        y: Float,
        p: ReceiptPaints
    ): Float {
        var curY = y

        // Invoice number and date on same line
        canvas.drawText("Invoice No : ${bill.invoiceNumber}", MARGIN, curY, p.bold12)
        val dateText = "${bill.invoiceDate} ${bill.invoiceTime}"
        canvas.drawText(
            dateText,
            PAGE_WIDTH - MARGIN - p.normal12.measureText(dateText),
            curY,
            p.normal12
        )

        curY += 35f

        // Shop name (centered, bold)
        canvas.drawText(shop.shopName, PAGE_WIDTH / 2, curY, p.title24)
        curY += 20f

        // Shop address and phone (centered, gray)
        canvas.drawText(
            "${shop.shopAddress} - ${shop.shopPhone}",
            PAGE_WIDTH / 2,
            curY,
            p.subTitle12
        )

        curY += 15f

        // Divider line
        canvas.drawLine(MARGIN, curY, PAGE_WIDTH - MARGIN, curY, p.line2)
        return curY + 25f
    }

    private fun drawCustomerAndItems(
        canvas: Canvas,
        bill: Bill,
        shop: ShopSettings,
        y: Float,
        p: ReceiptPaints
    ): Float {
        var curY = y

        // Customer info row
        canvas.drawText(bill.customerName, MARGIN + 50f, curY, p.bold14Center)
        canvas.drawText(bill.customerPhone, PAGE_WIDTH - MARGIN - 100f, curY, p.bold14Center)

        curY += 10f
        canvas.drawLine(MARGIN, curY, PAGE_WIDTH - MARGIN, curY, p.dotted)

        // Items table header
        curY += 20f
        val cols = floatArrayOf(
            MARGIN,
            PAGE_WIDTH * 0.45f,
            PAGE_WIDTH * 0.65f,
            PAGE_WIDTH - MARGIN
        )

        canvas.drawText("Item", cols[0] + 10f, curY, p.bold12)
        canvas.drawText("Qty", cols[1], curY, p.bold12Center)
        canvas.drawText("Price", cols[2], curY, p.bold12Center)
        canvas.drawText("Total", cols[3], curY, p.bold12Right)

        curY += 8f
        canvas.drawLine(MARGIN, curY, PAGE_WIDTH - MARGIN, curY, p.dotted)
        curY += 20f

        // Items rows
        bill.items.forEach { item ->
            canvas.drawText(item.itemName, cols[0] + 10f, curY, p.normal12)
            canvas.drawText(item.quantity.toString(), cols[1], curY, p.normal12Center)
            canvas.drawText(
                "${shop.currency} ${String.format(Locale.getDefault(), "%.0f", item.price)}",
                cols[2],
                curY,
                p.normal12Center
            )
            canvas.drawText(
                "${shop.currency} ${String.format(Locale.getDefault(), "%.0f", item.total)}",
                cols[3],
                curY,
                p.normal12Right
            )
            curY += 20f
        }

        canvas.drawLine(MARGIN, curY, PAGE_WIDTH - MARGIN, curY, p.line1)
        return curY + 25f
    }


    private fun drawSplitSection(
        canvas: Canvas,
        bill: Bill,
        shop: ShopSettings,
        y: Float,
        p: ReceiptPaints
    ): Float {
        val splitX = PAGE_WIDTH * 0.55f
        var leftY = y
        var rightY = y

        // LEFT: Prescription IMAGE (PRIORITY ORDER)
        val primaryPrescriptionImage = getPrimaryPrescriptionImage(bill)

        if (primaryPrescriptionImage != null) {
            val imageSize = splitX - MARGIN - 10f
            leftY = drawImageAt(
                canvas,
                primaryPrescriptionImage,
                MARGIN,
                leftY,
                imageSize,
                imageSize,
                "Prescription"
            )
        }

        // RIGHT: Calculations
        rightY = drawCalculations(canvas, bill, shop, PAGE_WIDTH - MARGIN, rightY, p)

        return maxOf(leftY, rightY) + 20f
    }

    private fun getPrimaryPrescriptionImage(bill: Bill): String? {
        // Priority 1: Check if there's a new prescription (form or image)
        bill.prescriptionFormData?.prescriptionImagePath?.let { path ->
            if (path.isNotBlank()) {
                return path
            }
        }

        // Priority 2: Use first saved prescription
        return bill.prescriptionImagesPaths.firstOrNull()
    }


    private fun drawImageGallery(
        canvas: Canvas,
        bill: Bill,
        y: Float,
        p: ReceiptPaints
    ): Float {
        val galleryImages = mutableListOf<String>()

        // Get the primary image that was already displayed
        val primaryImage = getPrimaryPrescriptionImage(bill)

        // Add saved prescription images (excluding the one already shown)
        val savedPrescriptions = if (primaryImage != null) {
            // If primary was from saved prescriptions, skip it
            bill.prescriptionImagesPaths.filter { it != primaryImage }
        } else {
            // If no primary was shown, include all saved prescriptions
            bill.prescriptionImagesPaths
        }
        galleryImages.addAll(savedPrescriptions)

        // Add all item images
        galleryImages.addAll(bill.imagesPaths)

        if (galleryImages.isEmpty()) return y

        var curY = y
        canvas.drawText("Attached Images:", MARGIN, curY, p.bold12)
        curY += 15f

        // Calculate grid layout
        val count = galleryImages.size
        val colCount = if (count <= 2) 2 else 3
        val spacing = 10f
        val imageSize = (PAGE_WIDTH - 2 * MARGIN - (colCount - 1) * spacing) / colCount

        galleryImages.forEachIndexed { index, path ->
            val row = index / colCount
            val col = index % colCount
            val imgX = MARGIN + col * (imageSize + spacing)
            val imgY = curY + row * (imageSize + spacing)

            drawImageAt(canvas, path, imgX, imgY, imageSize, imageSize)

            // Update curY after last image
            if (index == galleryImages.size - 1) {
                curY = imgY + imageSize + 20f
            }
        }

        return curY
    }

    private fun drawCalculations(
        canvas: Canvas,
        bill: Bill,
        shop: ShopSettings,
        rightX: Float,
        y: Float,
        p: ReceiptPaints
    ): Float {
        var curY = y
        val labelX = rightX - 90f
        val currency = shop.currency

        fun drawRow(
            label: String,
            value: Double,
            isMinus: Boolean = false,
            date: String? = null
        ) {
            canvas.drawText("$label $currency", labelX, curY, p.normal12Right)
            val prefix = if (isMinus) "- " else ""
            canvas.drawText(
                "$prefix${String.format(Locale.getDefault(), "%.0f", value)}",
                rightX,
                curY,
                p.bold12Right
            )
            curY += LINE_SPACING

            // Show date if provided
            date?.let {
                canvas.drawText(it, rightX, curY - 5f, p.gray9Right)
                curY += 12f
            }
        }

        // Draw all amounts
        drawRow("Total", bill.totalAmount)
        if (bill.previousAmount > 0) drawRow("Previous", bill.previousAmount)
        if (bill.discount > 0) drawRow("Discount", bill.discount, true)
        if (bill.advance > 0) drawRow("Advance", bill.advance, true)
        if (bill.advance2 > 0) drawRow("2nd Amount", bill.advance2, true, bill.advance2Date)
        if (bill.advance3 > 0) drawRow("3rd Amount", bill.advance3, true, bill.advance3Date)

        // Divider line
        curY += 5f
        canvas.drawLine(labelX - 20f, curY, rightX, curY, p.line2)
        curY += 20f

        // Remaining amount
        canvas.drawText("Remaining $currency", labelX, curY, p.bold14Right)
        val remainingColor = if (bill.remainingAmount > 0) Color.RED else "#4CAF50".toColorInt()
        canvas.drawText(
            String.format(Locale.getDefault(), "%.0f", bill.remainingAmount),
            rightX,
            curY,
            p.bold14Right.apply { this.color = remainingColor }
        )

        // Paid stamp
        if (!bill.isUnpaid) {
            curY += 35f
            val rect = RectF(rightX - 110f, curY - 22f, rightX, curY + 8f)
            canvas.drawRoundRect(rect, 6f, 6f, p.fillBlack)
            canvas.drawText("Cash Received", rect.centerX(), curY, p.bold12WhiteCenter)
        }

        // Pickup date
        if (bill.pickupDate != null) {
            curY += 30f
            canvas.drawText("Pickup: ${bill.pickupDate}", rightX, curY, p.bold11ItalicRight)
        }

        return curY + 15f
    }

    private fun drawFooter(
        canvas: Canvas,
        shop: ShopSettings,
        bottomY: Float,
        p: ReceiptPaints
    ) {
        var curY = bottomY - 40f
        canvas.drawText(
            "Thank you for your business!",
            PAGE_WIDTH / 2,
            curY,
            p.bold16ItalicCenter
        )

        if (shop.termsAndConditions.isNotBlank()) {
            curY -= 20f
            val lines = wrapText(shop.termsAndConditions, PAGE_WIDTH - 2 * MARGIN, p.gray10Center)
            lines.reversed().forEach { line ->
                canvas.drawText(line, PAGE_WIDTH / 2, curY, p.gray10Center)
                curY -= 14f
            }
        }
    }


    private fun drawImageAt(
        canvas: Canvas,
        path: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        label: String? = null,
        cornerRadius: Float = 18f
    ): Float {
        try {
            val file = File(path)
            if (!file.exists()) return y + h + 10f

            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return y + h + 10f

            val rect = RectF(x, y, x + w, y + h)

            // Save canvas state
            val saveCount = canvas.save()

            // Clip image with rounded corners
            val clipPath = Path().apply {
                addRoundRect(
                    rect,
                    cornerRadius,
                    cornerRadius,
                    Path.Direction.CW
                )
            }
            canvas.clipPath(clipPath)

            // Draw bitmap
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, null, rect, paint)

            // Restore canvas
            canvas.restoreToCount(saveCount)

            // Draw label if needed
            if (label != null) {
                val labelPaint = Paint().apply {
                    textSize = 10f
                    color = Color.DKGRAY
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                canvas.drawText(label, x + 5f, y + h + 15f, labelPaint)
            }

            bitmap.recycle()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (label != null) y + h + 25f else y + h + 10f
    }


    private fun calculateHeight(bill: Bill, shop: ShopSettings): Float {
        var h = 350f // Header + Customer base height

        // Items section height
        h += bill.items.size * 22f

        // Split section (prescription + calculations)
        val hasPrimaryImage = getPrimaryPrescriptionImage(bill) != null
        val splitHeight = if (hasPrimaryImage) 220f else 180f
        h += splitHeight

        // Gallery images calculation
        val primaryImage = getPrimaryPrescriptionImage(bill)
        val galleryCount = bill.prescriptionImagesPaths.filter { it != primaryImage }.size +
                bill.imagesPaths.size

        var rowCount = 0
        if (galleryCount > 0) {
            val colCount = if (galleryCount <= 2) 2 else 3
            val spacing = 10f
            val imageSize = (PAGE_WIDTH - 2 * MARGIN - (colCount - 1) * spacing) / colCount
            rowCount = (galleryCount + colCount - 1) / colCount

            h += 50f
            h += rowCount * (imageSize + spacing)
        }

        // Logic to decrease spacing as rows increase
        val spacingAfterGallery = when {
            rowCount == 0 -> 20f
            rowCount == 1 -> 20f
            rowCount == 2 -> 10f
            else -> 5f
        }
        h += spacingAfterGallery

        // Terms height (calculated dynamically based on line count)
        if (shop.termsAndConditions.isNotBlank()) {
            val paint = Paint().apply { textSize = 10f }
            val lines = wrapText(shop.termsAndConditions, PAGE_WIDTH - 2 * MARGIN, paint)
            h += lines.size * 14f + 20f
        }

        // Dynamic Bottom Margin (Thank you section + final buffer)
        // We reduce this buffer as the content gets longer
        val bottomMargin = when {
            rowCount <= 1 -> 100f
            rowCount == 2 -> 60f
            rowCount == 3 -> 40f
            else -> 30f // 4 rows or more
        }
        h += bottomMargin

        return h
    }

    private fun wrapText(text: String, width: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        for (word in words) {
            val testLine = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(testLine) < width) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(word)
            } else {
                if (current.isNotEmpty()) {
                    lines.add(current.toString())
                }
                current = StringBuilder(word)
            }
        }

        if (current.isNotEmpty()) {
            lines.add(current.toString())
        }

        return lines
    }

    private class ReceiptPaints {
        val title24 = Paint().apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val subTitle12 = Paint().apply {
            textSize = 12f
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
        }

        val bold12 = Paint().apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }

        val bold12Right = Paint().apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
        }

        val bold12WhiteCenter = Paint().apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }

        val bold12Center = Paint().apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val bold14Center = Paint().apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val bold14Right = Paint().apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
        }

        val bold11ItalicRight = Paint().apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.RIGHT
        }

        val normal12 = Paint().apply {
            textSize = 12f
        }

        val normal12Center = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }

        val normal12Right = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }

        val gray9Right = Paint().apply {
            textSize = 9f
            color = Color.GRAY
            textAlign = Paint.Align.RIGHT
        }

        val gray10Center = Paint().apply {
            textSize = 10f
            color = Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }

        val bold16ItalicCenter = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.CENTER
        }

        val line2 = Paint().apply {
            strokeWidth = 2f
            color = Color.BLACK
        }

        val line1 = Paint().apply {
            strokeWidth = 1f
            color = Color.BLACK
        }

        val dotted = Paint().apply {
            pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val fillBlack = Paint().apply {
            style = Paint.Style.FILL
            color = Color.BLACK
        }
    }
}
