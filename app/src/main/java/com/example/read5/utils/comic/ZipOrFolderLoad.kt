package com.example.read5.utils.comic

import android.content.Context
import android.util.Log
import com.example.read5.bean.ComicPage
import com.example.read5.utils.comic.GetImageSize.getFolderComicPages
import com.example.read5.utils.comic.GetImageSize.getImageSizeFromZipEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipFile

//获取目录信息,即图片信息,它顺序,地址等
class ZipOrFolderLoad(context: Context, path: String) {
    val TAG = "LoadComicPages"

    private val context = context
    private val path = path

    suspend fun loadComic(): List<ComicPage> {
        return try {
            if (path.substringAfterLast('.', "").lowercase() == "zip") {
                loadZipComicPages()
            } else {
                loadFolderComicPages()
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载漫画失败: $path", e)
            emptyList() // 返回空列表，而不是崩溃
        }
    }

    private suspend fun loadFolderComicPages(): List<ComicPage> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = File(path)
                if (!folder.exists() || !folder.isDirectory) {
                    Log.e(TAG, "文件夹不存在或不是目录: $path")
                    return@withContext emptyList()
                }

                val imageFiles = folder.listFiles()
                if (imageFiles == null || imageFiles.isEmpty()) {
                    Log.e(TAG, "文件夹为空: $path")
                    return@withContext emptyList()
                }

                // 转为 List，排序，再映射为 ComicPage
                imageFiles
                    .asList()
                    .sortedWith(compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name })
                    .mapNotNull { file ->  // 使用 mapNotNull 过滤掉无效图片
                        try {
                            val (width, height) = getFolderComicPages(file)
                            ComicPage(
                                uri = null,
                                name = file.name,
                                width = width,
                                height = height
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "处理图片失败: ${file.name}", e)
                            null // 跳过损坏的图片
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "加载文件夹失败: $path", e)
                emptyList()
            }
        }
    }

    private suspend fun loadZipComicPages(): List<ComicPage> = withContext(Dispatchers.IO) {
        var zip: ZipFile? = null
        try {
            // 1. 先检查文件是否存在
            val file = File(path)
            if (!file.exists()) {
                Log.e(TAG, "ZIP文件不存在: $path")
                return@withContext emptyList()
            }

            // 2. 检查文件是否可读
            if (!file.canRead()) {
                Log.e(TAG, "ZIP文件不可读: $path")
                return@withContext emptyList()
            }

            // 3. 尝试打开ZIP文件
            zip = try {
                ZipFile(file)
            } catch (e: ZipException) {
                Log.e(TAG, "无效的ZIP文件: $path", e)
                return@withContext emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "打开ZIP文件失败: $path", e)
                return@withContext emptyList()
            }

            // 4. 检查ZIP文件是否为空
            val entries = zip?.entries() ?: return@withContext emptyList()
            if (!entries.hasMoreElements()) {
                Log.e(TAG, "ZIP文件为空: $path")
                return@withContext emptyList()
            }

            // 5. 处理ZIP条目 - ✅ 修复：正确返回结果
            val result = zip.entries()
                .asSequence()
                .filter { !it.isDirectory && isValidImageName(it.name) }
                .mapNotNull { entry ->
                    try {
                        val (width, height) = getImageSizeFromZipEntry(zip, entry)
                        ComicPage(
                            name = entry.name,
                            width = width,
                            height = height,
                            uri = null
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "处理ZIP条目失败: ${entry.name}", e)
                        null // 跳过损坏的图片
                    }
                }
                .toList()
                .sortedWith(compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name })

            Log.d(TAG, "成功加载 ${result.size} 张图片")
            return@withContext result

        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "内存不足，无法加载ZIP: $path", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "加载ZIP失败: $path", e)
            emptyList()
        } finally {
            // 确保ZIP文件被关闭
            try {
                zip?.close()
            } catch (e: Exception) {
                Log.e(TAG, "关闭ZIP文件失败", e)
            }
        }
    }

    private fun isValidImageName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    }
}