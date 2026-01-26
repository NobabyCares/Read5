// 文件路径: app/src/main/java/com/example/read5/utils/ComicCoverExtractor.kt
package com.example.read5.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

object ComicCoverExtractor {
    private const val TAG = "ComicCoverExtractor"
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")

    /**
     * 提取漫画封面（支持文件夹 或 .zip/.cbz）
     * @param context 必需（用于 content:// 访问）
     * @param path 文件夹路径 或 压缩包路径（可为 "content://"）
     *             支持两种格式：
     *             1. 纯 tree URI: content://.../tree/...
     *             2. 拼接格式: treeUri|folderDocumentId （用于定位子目录）
     * @return 封面 Bitmap，失败返回 null
     */
    suspend fun extractCover(context: Context, path: String): Bitmap? = withContext(Dispatchers.IO) {
        Log.w(TAG, "Received path:  $ path")

        // 👇 新增：SAF 权限恢复
        if (path.startsWith("content://")) {
            val treeUri = if (path.contains('|')) {
                Uri.parse(path.split('|')[0]) // 提取 treeUri 部分
            } else {
                Uri.parse(path)
            }
            if (DocumentsContract.isTreeUri(treeUri)) {
                if (!restoreSafPermission(context, treeUri.toString())) {
                    Log.e(TAG, "Failed to restore permission for  $ treeUri")
                    return@withContext null
                }
            }
        }

        Log.w(TAG, "Received path: $path")
        Log.w(TAG, "Is tree URI? ${path.contains("/tree/")}")
        Log.w(TAG, "extractCover called with path: $path")
        return@withContext try {
            if (path.startsWith("content://")) {
                // === 新增：支持 treeUri|folderDocumentId 格式 ===
                if (path.contains('|')) {
                    Log.d(TAG, "Path uses 'treeUri|folderId' format, parsing...")
                    extractFromSubFolderByStoredPath(context, path)
                } else {
                    Log.d(TAG, "Path is pure SAF URI, delegating to extractFromContentFolder")
                    extractFromContentFolder(context, Uri.parse(path))
                }
            } else {
                val file = File(path)
                Log.d(TAG, "Checking local path: $path, exists=${file.exists()}, isDirectory=${file.isDirectory}")
                if (!file.exists()) {
                    Log.w(TAG, "Local file does not exist")
                    return@withContext null
                }
                if (isZipArchive(path)) {
                    Log.d(TAG, "Path is ZIP/CBZ archive")
                    extractFromZip(path)
                } else if (file.isDirectory) {
                    Log.d(TAG, "Path is local directory")
                    extractFromLocalFolder(file)
                } else {
                    Log.w(TAG, "Path is neither directory nor zip")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in extractCover", e)
            null
        }
    }

    private fun extractFromSubFolderByStoredPath(context: Context, storedPath: String): Bitmap? {
        val parts = storedPath.split('|', limit = 2)
        if (parts.size != 2) {
            Log.e(TAG, "Invalid stored path format: expected 'treeUri|relativePath'")
            return null
        }

        val treeUriString = parts[0]
        val relativePath = parts[1]

        val treeUri = Uri.parse(treeUriString)
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: run {
            Log.e(TAG, "Failed to create DocumentFile from tree URI")
            return null
        }

        // 导航到目标子目录
        var current: DocumentFile = root
        for (segment in relativePath.split("/")) {
            if (segment.isEmpty()) continue
            val child = current.findFile(segment)
            if (child == null || !child.isDirectory) {
                Log.w(TAG, "Subfolder not found or not a directory: '$segment' in path '$relativePath'")
                return null
            }
            current = child
        }

        Log.d(TAG, "Successfully navigated to subfolder: $relativePath")
        return findCoverInGivenFolder(context, current)
    }

    // ==================== 重构：通用方法，用于任意 DocumentFile 目录 ====================
    private fun findCoverInGivenFolder(context: Context, folder: DocumentFile): Bitmap? {
        val children = folder.listFiles()
        Log.d(TAG, "SAF: Found ${children?.size ?: 0} children in folder: ${folder.name}")

        if (children == null || children.isEmpty()) {
            Log.w(TAG, "SAF: No children found in the target folder")
            return null
        }

        // 打印所有子项用于诊断
        children.forEachIndexed { index, doc ->
//            Log.d(TAG, "SAF Child [$index]: name=${doc.name}, isFile=${doc.isFile}, type=${doc.type}")
        }

        val imageDocs = children.filter { doc ->
            doc.isFile && isValidImageName(doc.name)
        }.sortedBy { it.name?.lowercase() ?: "" }

        Log.d(TAG, "SAF: Found ${imageDocs.size} image files")

        if (imageDocs.isEmpty()) {
            Log.w(TAG, "SAF: No valid image files found (check extensions: $IMAGE_EXTENSIONS)")
            return null
        }

        val firstDoc = imageDocs.first()
        Log.d(TAG, "SAF: Selected first image: ${firstDoc.name}")

        return try {
            context.contentResolver.openInputStream(firstDoc.uri)?.use { stream ->
                Log.d(TAG, "SAF: Successfully opened input stream for: ${firstDoc.name}")
                decodeBitmapFromInputStream(stream)
            } ?: run {
                Log.e(TAG, "SAF: openInputStream() returned null")
                null
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SAF: SecurityException - Permission denied! Did you persist the tree URI permission?", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "SAF: Failed to decode bitmap", e)
            null
        }
    }

    // --- 2. Content URI 文件夹 (SAF) —— 现在只处理纯 tree URI（根目录）---
    private fun extractFromContentFolder(context: Context, uri: Uri): Bitmap? {
        // 诊断：检查该 URI 是否有持久权限
        val hasPersisted = context.contentResolver.persistedUriPermissions.any { it.uri == uri }
        Log.d(TAG, "SAF: URI has persisted permission: $hasPersisted")
        if (!hasPersisted) {
            Log.w(TAG, "⚠️ URI permission NOT persisted! User must re-select directory.")
        }

        Log.d(TAG, "SAF: Parsing tree URI: $uri")
        val tree = DocumentFile.fromTreeUri(context, uri) ?: run {
            Log.e(TAG, "Failed to create DocumentFile from URI. Possible causes:")
            Log.e(TAG, "1. URI is not a valid tree URI")
            Log.e(TAG, "2. App lacks persisted permission (did you call takePersistableUriPermission?)")
            return null
        }

        Log.d(TAG, "SAF: Tree document name: ${tree.name}, canRead=${tree.canRead()}, isDirectory=${tree.isDirectory}")
        // 复用通用逻辑
        return findCoverInGivenFolder(context, tree)
    }

    // ==================== PRIVATE IMPLEMENTATIONS (其余部分保持不变) ====================

    private fun isZipArchive(path: String): Boolean {
        val lowerPath = path.lowercase()
        return lowerPath.endsWith(".zip") || lowerPath.endsWith(".cbz")
    }

    // --- 1. 本地文件夹 ---
    private fun extractFromLocalFolder(dir: File): Bitmap? {
        if (!dir.exists() || !dir.isDirectory) {
            Log.w(TAG, "Local dir invalid: ${dir.absolutePath}")
            return null
        }
        val imageFiles = dir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in IMAGE_EXTENSIONS }
            ?: run {
                Log.w(TAG, "listFiles() returned null for: ${dir.absolutePath}")
                return null
            }
        if (imageFiles.isEmpty()) {
            Log.w(TAG, "No image files found in: ${dir.absolutePath}")
            return null
        }
        val firstImage = imageFiles.sortedBy { it.name.lowercase() }.firstOrNull()!!
        Log.d(TAG, "Selected first image: ${firstImage.name}")
        return decodeBitmapFromInputStream(firstImage.inputStream())
    }

    // --- 3. ZIP / CBZ ---
    private fun extractFromZip(zipPath: String): Bitmap? {
        if (!File(zipPath).exists()) {
            Log.w(TAG, "ZIP file does not exist: $zipPath")
            return null
        }
        return try {
            ZipFile(zipPath).use { zip ->
                val imageEntries = zip.entries()
                    .asSequence()
                    .filter { !it.isDirectory && isValidImageName(it.name) }
                    .sortedBy { it.name.lowercase() }
                    .toList()
                Log.d(TAG, "ZIP: Found ${imageEntries.size} image entries")
                if (imageEntries.isEmpty()) {
                    Log.w(TAG, "ZIP: No image entries found")
                    return null
                }
                val firstEntry = imageEntries.first()
                Log.d(TAG, "ZIP: Selected first entry: ${firstEntry.name}")
                // First pass: check bounds
                zip.getInputStream(firstEntry).use { stream ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(stream, null, options)
                    if (options.outWidth <= 0 || options.outHeight <= 0) {
                        Log.w(TAG, "ZIP: Invalid image dimensions")
                        return null
                    }
                    val inSampleSize = calculateInSampleSize(options, 300, 300)
                    // Second pass: decode
                    zip.getInputStream(firstEntry).use { realStream ->
                        val decodeOptions = BitmapFactory.Options().apply {
                            this.inSampleSize = inSampleSize
                            inPreferredConfig = Bitmap.Config.RGB_565
                        }
                        val bitmap = BitmapFactory.decodeStream(realStream, null, decodeOptions)
                        Log.d(TAG, "ZIP: Decoded bitmap ${bitmap?.width}x${bitmap?.height}")
                        bitmap
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ZIP: Failed to extract cover", e)
            null
        }
    }

    private fun decodeBitmapFromInputStream(stream: InputStream): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                // 可选：防止 OOM，强制缩放
                inSampleSize = 2
            }
            BitmapFactory.decodeStream(stream, null, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from stream", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun isValidImageName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in IMAGE_EXTENSIONS
    }
}