package com.example.read5.global

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.read5.bean.ItemInfo
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileScanner {
    private const val SAMPLE_SIZE = 4096 // 4KB
    private const val ROOT_PATH = "storage/emulated/0/"

    fun findBooksByExtensions(
        extensions: Set<String> = setOf("epub", "mobi", "pdf"),
        category: Long
    ): List<ItemInfo> {
        val result = mutableListOf<ItemInfo>()
        val queue = ArrayDeque<File>()
        val root = File(ROOT_PATH)
        if (!root.exists() || !root.isDirectory) return result
        root.listFiles()?.forEach { queue.add(it) }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current.isDirectory) {
                current.listFiles()?.forEach { child -> queue.add(child) }
            } else {
                val ext = getExtension(current.name).lowercase()
                if (ext in extensions) {
                    val lastModifiedStr = formatter.format(Date(current.lastModified()))
                    val hash = calculateContentBasedHash(current)
                    result += ItemInfo(
                        name = current.nameWithoutExtension,
                        path = current.absolutePath,
                        fileSize = current.length(),
                        fileType = ext,
                        createTime = lastModifiedStr,
                        hash = hash,
                        category = category,
                        isShow = true,
                    )
                }
            }
        }
        return result
    }

    private fun getExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < filename.length - 1) {
            filename.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }

    private fun calculateContentBasedHash(file: File): String {
        return try {
            val fileSize = file.length()
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(SAMPLE_SIZE)
                var totalRead = 0
                while (totalRead < SAMPLE_SIZE) {
                    val toRead = SAMPLE_SIZE - totalRead
                    val bytesRead = input.read(buffer, 0, toRead)
                    if (bytesRead == -1) break
                    digest.update(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
                if (fileSize > SAMPLE_SIZE * 2L) {
                    input.skip(fileSize - SAMPLE_SIZE * 2L - totalRead.toLong())
                    var readFromEnd = 0
                    while (readFromEnd < SAMPLE_SIZE) {
                        val toRead = SAMPLE_SIZE - readFromEnd
                        val bytesRead = input.read(buffer, 0, toRead)
                        if (bytesRead == -1) break
                        digest.update(buffer, 0, bytesRead)
                        readFromEnd += bytesRead
                    }
                }
                digest.update(fileSize.toString().toByteArray(Charsets.UTF_8))
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            val fallbackBytes = "${file.length()}-${file.lastModified()}".toByteArray(Charsets.UTF_8)
            MessageDigest.getInstance("SHA-256").digest(fallbackBytes)
                .joinToString("") { "%02x".format(it) }
        }
    }

    // ✅ 修复版：SAF 漫画扫描
    fun findComicBooksBySaf(
        context: Context,
        treeUri: Uri, // 必须是用户通过 OpenDocumentTree 选择的原始 URI
        categoryId: Long
    ): List<ItemInfo> {
        val result = mutableListOf<ItemInfo>()
        val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

        // 获取根 documentId（如 primary:Comics）
        val rootDocumentId = try {
            DocumentsContract.getDocumentId(treeUri)
        } catch (e: IllegalArgumentException) {
            // 手动解析：从 path 中提取最后一段
            // URI: content://.../tree/primary%3Acartoon → 我们要 "primary:cartoon"
            val path = treeUri.path ?: throw e
            val segments = path.split("/").filter { it.isNotEmpty() }
            if (segments.size >= 2 && segments[segments.size - 2] == "tree") {
                Uri.decode(segments.last()) // 解码 %3A → :
            } else {
                throw e
            }
        }

        // 列出根目录下的一级子项（只处理文件夹）
        listChildren(context, treeUri, rootDocumentId)?.forEach { child ->
            if (child.mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                processFolderIfComic(
                    context = context,
                    treeUri = treeUri,
                    folderDocumentId = child.documentId,
                    folderName = child.displayName ?: "Unknown",
                    imageExtensions = imageExtensions,
                    categoryId = categoryId,
                    result = result
                )
            }
        }

        return result
    }

    private fun processFolderIfComic(
        context: Context,
        treeUri: Uri,
        folderDocumentId: String,
        folderName: String,
        imageExtensions: Set<String>,
        categoryId: Long,
        result: MutableList<ItemInfo>
    ) {
        var totalFiles = 0
        var imageFiles = 0
        var firstImageDocId: String? = null

        listChildren(context, treeUri, folderDocumentId)?.forEach { child ->
            if (child.mimeType != DocumentsContract.Document.MIME_TYPE_DIR) {
                totalFiles++
                val ext = getExtension(child.displayName ?: "").lowercase()
                if (ext in imageExtensions) {
                    imageFiles++
                    if (firstImageDocId == null) {
                        firstImageDocId = child.documentId
                    }
                }
            }
        }

        if (totalFiles > 0 && imageFiles * 100 >= totalFiles * 99) {
            val hash = firstImageDocId?.let { docId ->
                val fileUri = buildDocumentUri(treeUri, docId)
                calculateContentBasedHashFromUri(context, fileUri)
            } ?: ""

            // 存储方式：treeUri|folderDocumentId，便于后续重建 URI
            val storedPath = "$treeUri|$folderDocumentId"

            result += ItemInfo(
                name = folderName,
                path = storedPath,
                fileSize = 0L,
                fileType = "comic",
                createTime = "",
                hash = hash,
                category = categoryId,
                isShow = true
            )
        }
    }

    // ✅ 正确的 listChildren：基于原始 treeUri + documentId
    private fun listChildren(
        context: Context,
        treeUri: Uri,
        parentDocumentId: String
    ): List<Document>? {
        return try {
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                parentDocumentId
            )

            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val documents = mutableListOf<Document>()
                while (cursor.moveToNext()) {
                    val docId = cursor.getString(0)
                    val name = cursor.getString(1)
                    val mime = cursor.getString(2)
                    documents.add(Document(docId, name, mime))
                }
                documents
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ 从 treeUri + docId 构建可读取的 URI
    private fun buildDocumentUri(treeUri: Uri, documentId: String): Uri {
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    }

    // ✅ 从 URI 计算 hash（只读前 4KB）
    private fun calculateContentBasedHashFromUri(context: Context, uri: Uri): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            context.contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(4096)
                var totalRead = 0
                var bytesRead: Int = 0
                while (totalRead < 4096 && input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
                digest.update(totalRead.toString().toByteArray(Charsets.UTF_8))
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // ✅ 简化 Document 类（只存必要信息）
    private data class Document(
        val documentId: String,
        val displayName: String?,
        val mimeType: String?
    )
}