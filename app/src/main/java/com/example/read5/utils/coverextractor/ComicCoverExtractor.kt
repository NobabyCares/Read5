// 文件路径: app/src/main/java/com/example/read5/utils/ComicCoverExtractor.kt
package com.example.read5.utils.coverextractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.read5.utils.restoreSafPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

class ComicCoverExtractor: CoverExtractor {
    private  val TAG = "ComicCoverExtractor"
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")

    override suspend fun extractCover(path: String, coverFile: File): Boolean {
        // 提取 EPUB 封面
        val bitmap = extractCover(path)
        if (bitmap != null) {
            // 保存为与 PDF 相同的格式（WebP/PNG）
            val saved = CoverExtractorUitils.saveCoverBitmap(bitmap = bitmap, coverFile)
            bitmap.recycle()
            return true
        }
        return false
    }

    /**
     * 提取漫画封面（支持文件夹 或 .zip/.cbz）
     * @param context 必需（用于 content:// 访问）

     * @return 封面 Bitmap，失败返回 null
     */
    suspend fun extractCover(path: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {

                val file = File(path)

                if (!file.exists()) {
                    return@withContext null
                }

                if (isZipArchive(path)) {
                    extractFromZip(file)
                } else if (file.isDirectory) {
                    extractFromLocalFolder(file)
                } else {
                    null
                }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in extractCover", e)
            null
        }
    }



//压缩包
    private fun isZipArchive(path: String): Boolean {
        val lowerPath = path.lowercase()
        return lowerPath.endsWith(".zip") || lowerPath.endsWith(".cbz")
    }

    // --- 1. 本地文件夹 ---
    private fun extractFromLocalFolder(dir: File): Bitmap? {
        // 检查目录是否存在且是一个目录
        if (!dir.exists() || !dir.isDirectory) return null

        // 获取所有图片文件并排序
        val firstImageFile = dir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in IMAGE_EXTENSIONS }
            ?.sortedBy { it.name.lowercase() }
            ?.firstOrNull()

        // 如果没有找到合适的图片文件，则返回 null
        if (firstImageFile == null) return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(firstImageFile.inputStream(), null, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null
        }
        val inSampleSize = calculateInSampleSize(options, 300, 300)
        return decodeBitmapFromInputStream(firstImageFile.inputStream(), inSampleSize)
        }

    // --- 3. ZIP / CBZ ---
    private fun extractFromZip(dir: File): Bitmap? {
        if (!dir.exists()) {
            return null
        }
        return try {
            ZipFile(dir).use { zip ->
                val imageEntries = zip.entries()
                    .asSequence()
                    .filter { !it.isDirectory && isValidImageName(it.name) }
                    .sortedBy { it.name.lowercase() }
                    .toList()
                if (imageEntries.isEmpty()) {
                    return null
                }
                val firstEntry = imageEntries.first()
                // First pass: check bounds
                zip.getInputStream(firstEntry).use { stream ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(stream, null, options)
                    if (options.outWidth <= 0 || options.outHeight <= 0) {
                        return null
                    }
                    val inSampleSize = calculateInSampleSize(options, 300, 300)
                    // Second pass: decode
                    zip.getInputStream(firstEntry).use { realStream ->
                       return decodeBitmapFromInputStream(realStream, inSampleSize)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ZIP: Failed to extract cover", e)
            null
        }
    }

    private fun decodeBitmapFromInputStream(file: InputStream, inSampleSize: Int): Bitmap?{
        // 第二遍：真实解码
        return file.use { stream ->
            BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }.let { decodeOptions ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }

    }

    private fun decodeBitmapFromInputStream(stream: InputStream): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                // 可选：防止 OOM，强制缩放
                inSampleSize = 2
            }
            BitmapFactory.decodeStream(stream, null, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from stream", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun isValidImageName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in IMAGE_EXTENSIONS
    }


}