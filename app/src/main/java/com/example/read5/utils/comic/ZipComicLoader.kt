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
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipFile
import kotlin.math.abs

class ZipComicLoader(
    private val zipPath: String,
    private val pageNames: List<ComicPage>, // ZIP 内所有图片文件名（已排序）
    private val cacheDir: File,
    private val scope: CoroutineScope
): ComicLoader {
        private val TAG = "LazyZipComicUtils"

        // 内存缓存：最近使用的 Bitmap（避免重复解码）
        private val memoryCache = ConcurrentHashMap<String, ImageBitmap>()

        // 磁盘缓存：已解压的文件（避免重复 I/O）
        private val diskCache = mutableSetOf<String>()

        private val MAX_PAGE_COUNT = 5


        init {
            cacheDir.mkdirs()
        }

        override suspend fun loadPage(index: Int): ImageBitmap? = withContext(Dispatchers.IO) {
            if (index !in pageNames.indices) return@withContext null

            val entryName = pageNames[index].name
            val cacheKey = "$index-$entryName"

            // 1. 先查内存缓存
            memoryCache[cacheKey]?.let { return@withContext it }

            // 2. 再查磁盘缓存（是否已解压）
            val cachedFile = File(cacheDir, sanitize(entryName))
            if (!diskCache.contains(entryName)) {
                // 3. 未缓存 → 解压到磁盘
                ZipFile(zipPath).use { zip ->
                    val entry = zip.getEntry(entryName) ?: return@withContext null
                    FileOutputStream(cachedFile).use { output ->
                        zip.getInputStream(entry).copyTo(output)
                    }
                }
                diskCache.add(entryName)
            }

            // 4. 从磁盘文件 decode Bitmap
            val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
                ?: return@withContext null

            val imageBitmap = bitmap.asImageBitmap()
            memoryCache[cacheKey] = imageBitmap

            // 5. 自动清理旧缓存（保留最近 5 页）
            if (memoryCache.size > MAX_PAGE_COUNT) {
                val keysToRemove = memoryCache.keys.filter { !isRecent(it, index) }
                keysToRemove.forEach { memoryCache.remove(it) }
            }

            imageBitmap
        }

        // 预加载前后 N 页
        fun preloadPages(currentIndex: Int, range: Int = 2) {
            for (i in (currentIndex - range)..(currentIndex + range)) {
                if (i in pageNames.indices) {
                    scope.launch {
                        loadPage(i) // 后台预加载
                    }
                }
            }
        }

        private fun isRecent(key: String, currentIndex: Int): Boolean {
            val pageIndex = key.substringBefore("-").toIntOrNull() ?: return false
            return abs(pageIndex - currentIndex) <= MAX_PAGE_COUNT
        }

        private fun sanitize(name: String): String {
            return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        }

        // 清理所有缓存（退出时调用）
        override suspend fun clearCache() {
            memoryCache.clear()
            diskCache.clear()
            cacheDir.deleteRecursively()
        }


}
