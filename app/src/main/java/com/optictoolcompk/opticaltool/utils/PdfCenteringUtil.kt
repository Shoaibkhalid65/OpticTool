package com.optictoolcompk.opticaltool.utils

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

object PdfCenteringUtil {

    fun createCenteredPdf(
        context: Context,
        sourceUri: Uri
    ): Uri {
        val renderer = PdfRenderer(
            context.contentResolver.openFileDescriptor(sourceUri, "r")!!
        )

        val outputFile = File(context.cacheDir, "centered_bill.pdf")

        val pdfDocument = PrintedPdfDocument(
            context,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )

        val pageWidth = pdfDocument.pageWidth
        val pageHeight = pdfDocument.pageHeight

        for (i in 0 until renderer.pageCount) {
            val sourcePage = renderer.openPage(i)
            val page = pdfDocument.startPage(i)

            val canvas = page.canvas

            val scale = minOf(
                pageWidth.toFloat() / sourcePage.width,
                pageHeight.toFloat() / sourcePage.height
            )

            val scaledWidth = sourcePage.width * scale
            val scaledHeight = sourcePage.height * scale

            val left = (pageWidth - scaledWidth) / 2f
            val top = (pageHeight - scaledHeight) / 2f

            canvas.save()
            canvas.translate(left, top)
            canvas.scale(scale, scale)

            val bitmap = createBitmap(sourcePage.width, sourcePage.height)

            sourcePage.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_PRINT
            )

            canvas.drawBitmap(bitmap, 0f, 0f, null)
            canvas.restore()

            pdfDocument.finishPage(page)

            bitmap.recycle()
            sourcePage.close()
        }

        pdfDocument.writeTo(FileOutputStream(outputFile))

        renderer.close()
        pdfDocument.close()

        return outputFile.toUri()
    }
}
