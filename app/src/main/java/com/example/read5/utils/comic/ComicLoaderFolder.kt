package com.example.read5.utils.comic

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.read5.bean.ComicPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ComicLoaderFolder(
    private val path : String,
    private val pageNames: List<ComicPage>,
) : ComicLoader {
    override suspend fun loadPage(index: Int): ImageBitmap?  = withContext(Dispatchers.IO) {
        // 1. 边界检查
        if (index !in pageNames.indices) return@withContext null

        // 2. 直接获取文件路径（假设 pageNames[index].name 是相对路径或文件名）
        val fileName = pageNames[index].name
        val imageFile = File(path, fileName) //

        // 3. 检查文件是否存在
        if (!imageFile.exists() || !imageFile.isFile) {
            return@withContext null
        }

        // ✅ 可选：如果你仍想用磁盘缓存（通常不需要！）
        // 对于本地文件，一般**不需要额外缓存**，因为文件 already on disk.
        // 但如果你要做格式转换/缩放，可以保留缓存逻辑。

        // 4. 直接解码图片（建议加 inSampleSize 防 OOM，见下方优化）
        BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap()
    }

    override suspend fun clearCache() {
    }
}