package com.example.read5.utils.comic

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.example.read5.bean.ComicPage
import com.example.read5.bean.PageLayout
import com.example.read5.bean.VirtualCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.Serializable
import java.util.zip.ZipFile


//构建虚拟页面zip格式
fun ZipbuilderVirtualCanvas(pages: List<ComicPage>): VirtualCanvas {
    var cumulativeTop = 0
    val layouts = mutableListOf<PageLayout>()

    // 遍历时获取索引位置
    pages.forEachIndexed { index, item ->

        layouts.add(
            PageLayout(
                index = index,
                top = cumulativeTop,
                width = item.width,
                height = item.height  // ← 不再是 0！
            )
        )

        cumulativeTop += item.height // 下一页的 top = 当前页 bottom
    }

    val totalWidth = layouts.maxOfOrNull { it.width } ?: 0
    val totalHeight = cumulativeTop // 所有页高度累加

    return VirtualCanvas(
        totalWidth = totalWidth,
        totalHeight = totalHeight,
        pageLayouts = layouts
    )
}

// ✅ 纯函数：输入已知尺寸的页面，输出虚拟画布
fun SAFbuildVirtualCanvas(pages: List<ComicPage>): VirtualCanvas {
    var cumulativeTop = 0
    val layouts = mutableListOf<PageLayout>()

    pages.forEachIndexed { index, page ->
        layouts.add(
            PageLayout(
                index = index,
                top = cumulativeTop,
                width = page.width,
                height = page.height
            )
        )
        cumulativeTop += page.height
    }

    val totalWidth = layouts.maxOfOrNull { it.width } ?: 0
    val totalHeight = cumulativeTop

    return VirtualCanvas(
        totalWidth = totalWidth,
        totalHeight = totalHeight,
        pageLayouts = layouts
    )
}


// 工具函数：从 Uri 获取尺寸（在 IO 线程调用）
fun getImageDimensionsFromUri(context: Context, uri: Uri): Pair<Int, Int> {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        return options.outWidth to options.outHeight
    } ?: return 0 to 0 // 如果无法打开输入流，则返回默认值
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
