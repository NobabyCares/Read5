package com.example.read5.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipFile

object EpubCoverExtractor {

    suspend fun extractCover(epubPath: String): Bitmap? = withContext(Dispatchers.IO) {
        var result: Bitmap? = null // 👈 关键：用变量收集结果
        try {
            ZipFile(epubPath).use { zip ->
                val opfPath = findOpfPath(zip) ?: return@withContext null
                val coverImagePath = parseCoverImagePath(zip, opfPath) ?: return@withContext null
                zip.getEntry(coverImagePath)?.let { entry ->
                    zip.getInputStream(entry).use { stream ->
                        result = BitmapFactory.decodeStream(stream)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // result 保持为 null
        }
        result // 👈 整个 lambda 的返回值
    }

    private fun findOpfPath(zip: ZipFile): String? {
        val containerEntry = zip.getEntry("META-INF/container.xml") ?: return null
        return zip.getInputStream(containerEntry).use { input ->
            val text = input.bufferedReader().readText()
            """full-path="([^"]+\.opf)"""".toRegex()
                .find(text)?.groupValues?.getOrNull(1)
        }
    }

    private fun parseCoverImagePath(zip: ZipFile, opfPath: String): String? {
        val opfEntry = zip.getEntry(opfPath) ?: return null
        val baseDir = opfPath.substringBeforeLast('/')

        zip.getInputStream(opfEntry).use { input ->
            val xml = input.bufferedReader().readText()

            // === 方法 1: 标准 meta cover ===
            val coverId = Regex("""<meta[^>]*name\s*=\s*["']cover["'][^>]*content\s*=\s*["']([^"']+)["']""")
                .find(xml)?.groupValues?.getOrNull(1)

            if (!coverId.isNullOrBlank()) {
                val href = Regex("""<item[^>]*id\s*=\s*["']$coverId["'][^>]*href\s*=\s*["']([^"']+)["']""")
                    .find(xml)?.groupValues?.getOrNull(1)
                if (href != null) {
                    return normalizePath(baseDir, href)
                }
            }

            // === 方法 2: 第一个 image 类型的 item ===
            val firstImageHref = Regex("""<item[^>]*media-type\s*=\s*["']image/[^"']*["'][^>]*href\s*=\s*["']([^"']+)["']""")
                .find(xml)?.groupValues?.getOrNull(1)
            if (firstImageHref != null) {
                return normalizePath(baseDir, firstImageHref)
            }

            // === 方法 3: 查找 cover.xhtml 或 titlepage.xhtml 中的 <img> ===
            val coverXhtmlPath = findCoverXhtmlPath(zip, opfPath, xml, baseDir)
            if (coverXhtmlPath != null) {
                return extractImgSrcFromXhtml(zip, coverXhtmlPath, baseDir)
            }

            return null
        }
    }

    // 辅助：标准化路径
    private fun normalizePath(baseDir: String, href: String): String {
        return if (href.startsWith("/")) href else "$baseDir/$href".replace("//", "/")
    }

    // 辅助：查找可能的封面 XHTML 文件
    private fun findCoverXhtmlPath(zip: ZipFile, opfPath: String, opfXml: String, baseDir: String): String? {
        // 尝试常见封面文件名
        val candidates = listOf("cover.xhtml", "titlepage.xhtml", "Cover.xhtml")
        for (candidate in candidates) {
            val fullPath = "$baseDir/$candidate".replace("//", "/")
            if (zip.getEntry(fullPath) != null) {
                return fullPath
            }
        }

        // 或从 spine 中找第一个 itemref
        val firstSpineId = Regex("""<itemref[^>]*idref\s*=\s*["']([^"']+)["']""")
            .find(opfXml)?.groupValues?.getOrNull(1)
        if (firstSpineId != null) {
            val href = Regex("""<item[^>]*id\s*=\s*["']$firstSpineId["'][^>]*href\s*=\s*["']([^"']+)["']""")
                .find(opfXml)?.groupValues?.getOrNull(1)
            if (href != null) {
                val xhtmlPath = normalizePath(baseDir, href)
                if (xhtmlPath.endsWith(".xhtml") || xhtmlPath.endsWith(".html")) {
                    return xhtmlPath
                }
            }
        }
        return null
    }

    // 辅助：从 XHTML 中提取 <img src="...">
    private fun extractImgSrcFromXhtml(zip: ZipFile, xhtmlPath: String, baseDir: String): String? {
        return zip.getEntry(xhtmlPath)?.let { entry ->
            zip.getInputStream(entry).use { stream ->
                val html = stream.bufferedReader().readText()
                // 匹配 <img src="...">，忽略大小写和属性顺序
                Regex("""<img[^>]*src\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                    .find(html)?.groupValues?.getOrNull(1)
                    ?.let { src ->
                        if (src.startsWith("http")) null // 忽略网络图
                        else normalizePath(xhtmlPath.substringBeforeLast('/'), src)
                    }
            }
        }
    }
}