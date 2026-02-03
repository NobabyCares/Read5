package com.example.read5.utils.coverextractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfCoverGenerator : CoverExtractor {

    private  val COVER_WIDTH = 300
    /** 生成 PDF 封面（支持 file path 和 content URI） */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override suspend fun extractCover(path: String, coverFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val pfd = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                pfd?.use { fd ->
                    PdfRenderer(fd).use { renderer ->
                        if (renderer.pageCount <= 0) return@withContext false

                        renderer.openPage(0).use { page ->
                            // 按比例缩放
                            val height = (page.height * COVER_WIDTH.toFloat() / page.width).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(COVER_WIDTH, height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

//                      保存封面
                            CoverExtractorUitils.saveCoverBitmap(bitmap, coverFile)

                            bitmap.recycle()
                        }
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }}
    }


}