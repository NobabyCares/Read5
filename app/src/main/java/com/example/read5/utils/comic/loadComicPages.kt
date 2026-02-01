package com.example.read5.utils.comic

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.read5.bean.ComicPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile


const val TAG = "LoadComicPages"

suspend fun loadComicPages(context: Context, path: String): List<ComicPage> {
    return if(path.startsWith("content://")){
        loadSafComic(context, path)
    }else {
        LoadZipComic(context, path)
    }
}


//SAF, 这里是content:://路径
suspend fun loadSafComic(context: Context, storedPath: String): List<ComicPage> {


    return withContext(Dispatchers.IO) {
        val parts = storedPath.split('|', limit = 2)
        if (parts.size != 2) throw IllegalArgumentException("Invalid path format")

        val treeUri = Uri.parse(parts[0])
        val folderDocumentId = parts[1]

        val root = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IllegalStateException("Failed to access tree URI")

        var current = root
        for (segment in folderDocumentId.split("/")) {
            if (segment.isEmpty()) continue
            val child = current.findFile(segment)
            if (child == null || !child.isDirectory) {
                throw IllegalStateException("Subfolder not found:  $segment") // ✅ 修复字符串模板
            }
            current = child
        }

        val children = current.listFiles() ?: emptyArray()
        val imageFiles = children.filter { it.isFile && isValidImageName(it.name) }
            .sortedBy { it.name?.lowercase() ?: "" }

        if (imageFiles.isEmpty()) throw IllegalStateException("No images found")

        imageFiles.map { file ->
            ComicPage(uri = file.uri, name = file.name ?: "unknown")
        }

    }
}

suspend fun LoadZipComic(context: Context, zipPath: String): List<ComicPage>{
    return ZipFile(zipPath).use { zipFile ->
        zipFile.entries().asSequence().map { entry ->
            ComicPage(
                name = entry.name,
                uri = Uri.parse("${zipPath}#${entry.name}"),
            )
        }.toList()
    }
}

private fun isValidImageName(name: String?): Boolean {
    if (name.isNullOrBlank()) return false
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
}