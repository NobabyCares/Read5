package com.example.read5.utils.comic

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.read5.bean.ComicPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipFile



class SafFolderLoad(context: Context, path: String) {
     val TAG = "LoadComicPages"

    private val context = context

    private val path = path

    suspend fun loadComic(): List<ComicPage> {
        return if(path.startsWith("content://")){
            loadSafComicPages()
        }else {
            loadZipComicPages()
        }
    }



    suspend fun loadSafComicPages(): List<ComicPage> {
        return withContext(Dispatchers.IO) {
            val parts = path.split('|', limit = 2)
            if (parts.size != 2) throw IllegalArgumentException("Invalid path format")

            val treeUri = Uri.parse(parts[0])
            val folderDocumentId = parts[1]

            val root = DocumentFile.fromTreeUri(context, treeUri)
                ?: throw IllegalStateException("Failed to access tree URI")

            var current = root
            for (segment in folderDocumentId.split("/")) {
                if (segment.isEmpty()) continue
                val child = current.findFile(segment)
                    ?: throw IllegalStateException("Subfolder not found: $segment")
                current = child
            }

            val children = current.listFiles() ?: emptyArray()
            val imageFiles = children.filter { it.isFile && isValidImageName(it.name) }
                .sortedBy { it.name?.lowercase() ?: "" }

            if (imageFiles.isEmpty()) throw IllegalStateException("No images found")

            imageFiles.map { file ->
                val (width, height) = getImageDimensionsFromUri(context, file.uri)
                ComicPage(uri = file.uri, name = file.name ?: "unknown", width = width, height = height)
            }
        }
    }


    suspend fun loadZipComicPages(): List<ComicPage> = withContext(Dispatchers.IO) {
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
        }
    }

    private fun isValidImageName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    }
}
