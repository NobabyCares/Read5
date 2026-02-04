package com.example.read5.cache

import android.util.Log
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap


class ComicPageCache(
    maxPages: Int = 15,
    // 👈 新增回调,通知父类重组
    private val onPut: ((Int) -> Unit)? = null  ) {

    companion object {
        const val MAX_PAGES = 15
    }
    private val TAG = "ComicPageCache"

    private val lruCache = object : LruCache<Int, ImageBitmap>(maxPages) {
        override fun sizeOf(key: Int, bitmap: ImageBitmap): Int {
            // 使用像素数量作为权重（更精确控制内存）
            return bitmap.width * bitmap.height
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: Int,
            oldValue: ImageBitmap,
            newValue: ImageBitmap?
        ) {
            if (evicted) {
                Log.d("ComicPageCache", "Evicted page $key")
            }
        }


    }



    fun get(pageIndex: Int): ImageBitmap? {
        return lruCache.get(pageIndex)
    }

    fun put(pageIndex: Int, bitmap: ImageBitmap) {
        lruCache.put(pageIndex, bitmap)
        // ✅ 手动触发回调（在调用 put 的线程）
        onPut?.invoke(pageIndex)
    }

    fun contains(pageIndex: Int): Boolean {
        return lruCache.snapshot().containsKey(pageIndex)
    }

    fun clear() {
        lruCache.evictAll()
    }

    fun size(): Int = lruCache.size()


}