package com.example.read5.utils.comic

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import com.example.read5.bean.ComicPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
class SafComicLoader(
    private val context: Context,
    private val imageFiles: List<ComicPage>
) {
    private val memoryCache = LruCache<Int, ImageBitmap>(5)

    suspend fun loadPage(index: Int): ImageBitmap? {
        if (index !in imageFiles.indices) return null

        // 1. 内存缓存
        memoryCache.get(index)?.let { return it }

        val uri = imageFiles[index].uri ?: return null

        return withContext(Dispatchers.IO) {
            try {
                // 2. 获取尺寸
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                // 3. 计算采样
                val targetWidth = (Resources.getSystem().displayMetrics.widthPixels * 1.5).toInt()
                val inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, targetWidth)

                // 4. 解码
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                        this.inSampleSize = inSampleSize
                    }
                    val bitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
                    bitmap?.asImageBitmap()?.also {
                        memoryCache.put(index, it)
                    }
                }
            } catch (e: Exception) {
                Log.e("SafComicLoader", "Failed to load page $index", e)
                null
            }
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int): Int {
        var inSampleSize = 1
        if (height > reqWidth || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqWidth && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun clearCache() {
        memoryCache.evictAll()
    }
}