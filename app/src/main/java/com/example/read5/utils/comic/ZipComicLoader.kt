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


        // 磁盘缓存：已解压的文件（避免重复 I/O）
        private val diskCache = mutableSetOf<String>()



        init {
            cacheDir.mkdirs()
        }

        override suspend fun loadPage(index: Int): ImageBitmap? = withContext(Dispatchers.IO) {
            if (index !in pageNames.indices) return@withContext null

            val entryName = pageNames[index].name
            val cachedFile = File(cacheDir, "page_$index.dat")

            // ✅ 关键：直接检查文件是否存在，不再用 diskCache 集合！
            if (!cachedFile.exists()) {
                ZipFile(zipPath).use { zip ->
                    val entry = zip.getEntry(entryName) ?: return@withContext null
                    FileOutputStream(cachedFile).use { output ->
                        zip.getInputStream(entry).copyTo(output)
                    }
                }
            }

            BitmapFactory.decodeFile(cachedFile.absolutePath)?.asImageBitmap()
        }

        private fun sanitize(name: String): String {
            return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        }

        // 清理所有缓存（退出时调用）
        override suspend fun clearCache() {
            diskCache.clear()
            cacheDir.deleteRecursively()
        }


}
