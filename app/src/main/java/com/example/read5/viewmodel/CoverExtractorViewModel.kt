package com.example.read5.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.read5.utils.coverextractor.ComicCoverExtractor
import com.example.read5.utils.coverextractor.CoverExtractor
import com.example.read5.utils.coverextractor.EpubCoverExtractor
import com.example.read5.utils.coverextractor.PdfCoverGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject


@HiltViewModel
class CoverExtractorViewModel @Inject constructor(
    @ApplicationContext private val c: Context  // ✅ 明确指定要 Application Contextc
): ViewModel() {
    private val COVER_DIR_NAME = "covers"

    private lateinit var coverExtractor: CoverExtractor

    val context = c

    private val dir: File = run {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val coverDir = File(baseDir, COVER_DIR_NAME)
        coverDir.mkdirs() // 确保目录存在
        coverDir
    }

    fun initCoverExtractor(filetype: String) {
        when (filetype.lowercase()) {
            "pdf" -> {
                coverExtractor = PdfCoverGenerator()
            }
            "epub" -> {
                // 提取 EPUB 封面
                coverExtractor = EpubCoverExtractor()
            }
            "folder", "zip", "cbz", "comic" -> {
                coverExtractor = ComicCoverExtractor()
            }
            // 可扩展 azw3 等
        }
    }

    /** 检查封面是否存在 */
    fun hasCover(hash: String): Boolean {
        return File(dir, "$hash.JPEG").exists()
    }

    /** 生成封面 */
    suspend  fun generateCover(path: String, hash: String): Boolean {
        val coverFile = File(dir, "$hash.JPEG")
        return coverExtractor.extractCover(path, coverFile)
    }


    fun getCoverFile(hash: String): File {
        return  File(dir, "$hash.JPEG")
    }



}