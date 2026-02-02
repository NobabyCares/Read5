package com.example.read5.utils.comic

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.read5.bean.ComicPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

class ZipComicLoader(
    private val zipPath: String,
    private val pageNames: List<ComicPage>,
    private val cacheDir: File,
    private val scope: CoroutineScope
) : ComicLoader {

    init {
        cacheDir.mkdirs()
    }

    companion object {
        private const val MAX_CACHE_BYTES = 100L * 1024 * 1024 // 100 MB
        private const val SAFE_THRESHOLD = 90L * 1024 * 1024   // 90 MB，留缓冲
    }

    override suspend fun loadPage(index: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        if (index !in pageNames.indices) return@withContext null

        val entryName = pageNames[index].name
        val cachedFile = File(cacheDir, "page_$index.dat")

        if (!cachedFile.exists()) {
            ZipFile(zipPath).use { zip ->
                val entry = zip.getEntry(entryName) ?: return@withContext null
                FileOutputStream(cachedFile).use { output ->
                    zip.getInputStream(entry).copyTo(output)
                }
            }

            // ✅ 新增：写入后触发缓存清理
            scope.launch(Dispatchers.IO) {
                trimCacheIfNeeded()
            }
        }

        BitmapFactory.decodeFile(cachedFile.absolutePath)?.asImageBitmap()
    }

    /**
     * 检查缓存目录总大小，如果超过阈值，按最后修改时间 LRU 删除旧文件
     */
    private fun trimCacheIfNeeded() {
        val files = cacheDir.listFiles()?.filter { it.isFile } ?: return
        var totalSize = files.sumOf { it.length() }

        if (totalSize <= SAFE_THRESHOLD) return

        // 按最后修改时间升序排序（最早修改的在前 → 最久未用）
        val sortedFiles = files.sortedBy { it.lastModified() }

        for (file in sortedFiles) {
            if (totalSize <= SAFE_THRESHOLD) break
            val fileSize = file.length()
            if (file.delete()) {
                totalSize -= fileSize
            }
        }
    }

    override suspend fun clearCache() {
        cacheDir.deleteRecursively()
    }
}