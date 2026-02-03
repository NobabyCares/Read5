package com.example.read5.utils.comic

import android.content.Context
import com.example.read5.bean.ComicPage
import com.example.read5.utils.comic.GetImageSize.getFolderComicPages
import com.example.read5.utils.comic.GetImageSize.getImageSizeFromZipEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile



class ZipOrFolderLoad(context: Context, path: String) {
     val TAG = "LoadComicPages"

    private val context = context

    private val path = path

    suspend fun loadComic(): List<ComicPage> {
        return if(path.substringAfterLast('.', "").lowercase() == "zip"){
            loadZipComicPages()
        }else {
            loadFolderComicPages()
        }
    }

    private suspend fun loadFolderComicPages(): List<ComicPage> {
        return withContext(Dispatchers.IO) {
            val folder = File(path)
            if (!folder.exists() || !folder.isDirectory) {
                return@withContext emptyList()
            }

            val imageFiles = folder.listFiles()

            // 转为 List，排序，再映射为 ComicPage
            imageFiles
                .asList()
                .sortedWith(compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name })
                .map { file ->
                    val (width, height) = getFolderComicPages(file)
                    ComicPage(
                        uri = null,
                        name = file.name,
                        width = width,
                        height = height
                    )
                }
        }
    }


    private suspend fun loadZipComicPages(): List<ComicPage> = withContext(Dispatchers.IO) {
        ZipFile(path).use { zip ->
            zip.entries()
                .asSequence()
                .filter { !it.isDirectory && isValidImageName(it.name) } // 只处理图片
                .map { entry ->
                    val (width, height) = getImageSizeFromZipEntry(zip, entry)
                    ComicPage(
                        name = entry.name,
                        width = width,
                        height = height,
                        uri = null // ZIP 模式无 Uri
                    )
                }
                .toList()
                .sortedWith(  compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name } )
        }
    }

    private fun isValidImageName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    }
}
