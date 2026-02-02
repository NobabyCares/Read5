package com.example.read5.utils.comic

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class ImageStitcher(private val loadPage: suspend (Int) -> ImageBitmap?) {

    /**
     * 垂直拼接 [startIndex] 到 [startIndex + count - 1] 的页面
     * @param startIndex 起始页索引
     * @param count 拼接页数（如 4）
     * @param loadPage 提供单页 ImageBitmap 的 suspend 函数
     * @return 拼接后的 ImageBitmap，失败返回 null
     */
    suspend fun stitchPagesVertically(
        startIndex: Int,
        count: Int,
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        var totalHeight = 0
        var maxWidth = 0

        // 加载所有页面的 Bitmap
        for (i in 0 until count) {
            val index = startIndex + i
            val imageBitmap = loadPage(index) ?: continue
            val bitmap = imageBitmap.asAndroidBitmap()
            bitmaps.add(bitmap)
            totalHeight += bitmap.height
            maxWidth = maxOf(maxWidth, bitmap.width)
        }

        if (bitmaps.isEmpty()) return@withContext null

        // 创建目标 Bitmap（使用 RGB_565 节省内存）
        val config = Bitmap.Config.RGB_565
        val stitchedBitmap = try {
            Bitmap.createBitmap(maxWidth, totalHeight, config)
        } catch (e: OutOfMemoryError) {
            // 内存不足，清理并返回 null
            bitmaps.forEach { it.recycle() }
            return@withContext null
        }

        // 绘制到画布
        val canvas = Canvas(stitchedBitmap)
        var currentY = 0
        for (bitmap in bitmaps) {
            // 居中对齐（如果宽度小于 maxWidth）
            val dx = (maxWidth - bitmap.width) / 2f
            canvas.drawBitmap(bitmap, dx, currentY.toFloat(), null)
            currentY += bitmap.height
            bitmap.recycle() // 立即释放，减少峰值内存
        }

        // 返回 Compose 可用的 ImageBitmap
        try {
            stitchedBitmap.asImageBitmap()
        } catch (e: Exception) {
            stitchedBitmap.recycle()
            null
        }
    }
}