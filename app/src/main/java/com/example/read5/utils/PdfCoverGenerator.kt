package com.example.read5.utils

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

object PdfCoverGenerator {

    private const val COVER_DIR_NAME = "covers"
    private const val COVER_WIDTH = 300 // 像素，平衡清晰度与内存

    /** 获取封面文件对象 */
    fun getCoverFile(context: Context, hash: String): File {
        val dir = File(context.filesDir, COVER_DIR_NAME)
        dir.mkdirs()
        return File(dir, "$hash.webp")
    }

    /** 检查封面是否存在 */
    fun hasCover(context: Context, hash: String): Boolean {
        return getCoverFile(context, hash).exists()
    }

    /** 生成 PDF 封面（支持 file path 和 content URI） */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun generatePdfCover(
        context: Context,
        source: String, // filePath: 可能是 "/sdcard/..." 或 "content://..."
        hash: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val coverFile = getCoverFile(context, hash)
            val pfd = if (source.startsWith("content://")) {
                // 处理 content URI
                val uri = Uri.parse(source)
                context.contentResolver.openFileDescriptor(uri, "r")
            } else {
                // 处理普通文件路径
                ParcelFileDescriptor.open(File(source), ParcelFileDescriptor.MODE_READ_ONLY)
            }

            pfd?.use { fd ->
                PdfRenderer(fd).use { renderer ->
                    if (renderer.pageCount <= 0) return@withContext false

                    renderer.openPage(0).use { page ->
                        // 按比例缩放
                        val height = (page.height * COVER_WIDTH.toFloat() / page.width).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(COVER_WIDTH, height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        // 保存为 WebP（高压缩、高质量）
                        FileOutputStream(coverFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
                        }
                        bitmap.recycle()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveCoverBitmap(context: Context, hash: String, bitmap: Bitmap): Boolean {
        return try {
            val coverFile = getCoverFile(context, hash)
            FileOutputStream(coverFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}