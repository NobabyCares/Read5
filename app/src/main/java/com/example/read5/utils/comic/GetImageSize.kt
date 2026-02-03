package com.example.read5.utils.comic

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipFile

object GetImageSize {
//    从文件夹里来获取尺寸
    fun getFolderComicPages(filepath: File): Pair<Int, Int> {
        if (!filepath.exists() || !filepath.isFile) {
            return Pair(0, 0)
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        // 使用 file.absolutePath —— 这是安全的
        BitmapFactory.decodeFile(filepath.absolutePath, options)

        return if (options.outWidth > 0 && options.outHeight > 0) {
            Pair(options.outWidth, options.outHeight)
        } else {
            Pair(0, 0)
        }
    }

    // 辅助：从 ZIP Entry 读取尺寸（高效，只读头部）
    fun getImageSizeFromZipEntry(zip: ZipFile, entry: java.util.zip.ZipEntry): Pair<Int, Int> {
        return try {
            val buffer = ByteArray(4096)
            zip.getInputStream(entry).use { input ->
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) return 0 to 0

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                ByteArrayInputStream(buffer, 0, bytesRead).use { bis ->
                    BitmapFactory.decodeStream(bis, null, options)
                }

                if (options.outWidth > 0 && options.outHeight > 0) {
                    options.outWidth to options.outHeight
                } else {
                    0 to 0 // 无法解析
                }
            }
        } catch (e: Exception) {
            Log.e("ZIP", "Failed to read ${entry.name}", e)
            0 to 0
        }
    }

}