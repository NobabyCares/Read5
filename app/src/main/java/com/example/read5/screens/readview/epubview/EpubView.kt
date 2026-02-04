// EpubView.kt
package com.example.read5.screens.readview

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.example.read5.singledata.DocumentHolder
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

private const val TAG = "EpubReader"

@Composable
fun EpubScreen() {
    var htmlContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val itemInfo = DocumentHolder.requireItem()
    val epubPath = itemInfo.path

    LaunchedEffect(epubPath) {
        if (epubPath.isNotEmpty()) {
            try {
                Log.d(TAG, "开始加载 EPUB: $epubPath")
                htmlContent = loadFirstChapterFromEpub(epubPath)
                Log.d(TAG, "EPUB 加载成功，HTML 长度: ${htmlContent.length}")
                errorMessage = null
            } catch (e: Exception) {
                Log.e(TAG, "加载 EPUB 失败", e)
                errorMessage = "加载失败: ${e.message}"
                htmlContent = """
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <style>
                            body {
                                font-family: sans-serif;
                                padding: 20px;
                                color: #333;
                            }
                            .error {
                                color: #d32f2f;
                                border: 1px solid #ffcdd2;
                                background: #ffebee;
                                padding: 20px;
                                border-radius: 8px;
                            }
                            pre {
                                background: #f5f5f5;
                                padding: 10px;
                                border-radius: 4px;
                                overflow: auto;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="error">
                            <h2>加载 EPUB 失败</h2>
                            <p>${e.message}</p>
                            <pre>${e.stackTraceToString()}</pre>
                        </div>
                    </body>
                    </html>
                """.trimIndent()
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        // 显示加载中
        LoadingView()
    } else if (errorMessage != null && htmlContent.isEmpty()) {
        // 显示错误
        ErrorView(errorMessage ?: "未知错误")
    } else {
        WebViewContent(
            htmlContent = htmlContent,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun LoadingView() {
    androidx.compose.material3.CircularProgressIndicator()
}

@Composable
private fun ErrorView(message: String) {
    androidx.compose.material3.Text(
        text = message,
        color = androidx.compose.material3.MaterialTheme.colorScheme.error
    )
}

@Composable
private fun WebViewContent(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(android.graphics.Color.WHITE)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "WebView 页面加载完成")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Log.e(TAG, "WebView 加载错误: $description ($errorCode) - $failingUrl")
                    }
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

// ====== 修复的 EPUB 解析逻辑 ======
private fun loadFirstChapterFromEpub(epubPath: String): String {
    val file = File(epubPath)
    if (!file.exists()) {
        throw IllegalStateException("EPUB 文件不存在: $epubPath")
    }

    Log.d(TAG, "打开 ZIP 文件: ${file.absolutePath}")
    ZipFile(file).use { zipFile ->
        // 1. 读取 container.xml
        val containerEntry = zipFile.getEntry("META-INF/container.xml")
            ?: throw IllegalStateException("EPUB 格式错误: 缺少 container.xml")

        val containerXml = zipFile.getInputStream(containerEntry).use { inputStream ->
            inputStream.readBytes().toString(StandardCharsets.UTF_8)
        }
        Log.d(TAG, "container.xml 读取成功，大小: ${containerXml.length}")

        // 2. 提取 OPF 文件路径
        val opfPath = extractOpfPath(containerXml)
        Log.d(TAG, "解析出 OPF 路径: $opfPath")

        // 3. 读取 OPF 文件
        val opfEntry = zipFile.getEntry(opfPath)
            ?: throw IllegalStateException("找不到 OPF 文件: $opfPath")

        val opfContent = zipFile.getInputStream(opfEntry).use { inputStream ->
            inputStream.readBytes().toString(StandardCharsets.UTF_8)
        }
        Log.d(TAG, "OPF 文件读取成功，大小: ${opfContent.length}")

        // 4. 解析 OPF 文件，获取第一章路径
        val (chapterRelativePath, opfBaseDir) = extractFirstChapterFromOpf(opfContent, opfPath)
        Log.d(TAG, "第一章相对路径: $chapterRelativePath, OPF 目录: $opfBaseDir")

        // 5. 构建完整的章节路径
        val chapterFullPath = if (opfBaseDir.isNotEmpty()) {
            "$opfBaseDir/$chapterRelativePath"
        } else {
            chapterRelativePath
        }.replace("//", "/")

        Log.d(TAG, "第一章完整路径: $chapterFullPath")

        // 6. 读取章节内容
        val chapterEntry = zipFile.getEntry(chapterFullPath)
            ?: throw IllegalStateException("找不到章节文件: $chapterFullPath")

        val chapterContent = zipFile.getInputStream(chapterEntry).use { inputStream ->
            inputStream.readBytes().toString(StandardCharsets.UTF_8)
        }
        Log.d(TAG, "章节内容读取成功，大小: ${chapterContent.length}")

        // 7. 清理和准备 HTML 内容
        return wrapHtmlWithStyles(chapterContent, opfBaseDir, zipFile)
    }
}

private fun extractOpfPath(containerXml: String): String {
    // 查找 full-path 属性
    val pattern = """full-path=["']([^"']+)["']""".toRegex()
    val match = pattern.find(containerXml)

    if (match == null) {
        Log.e(TAG, "container.xml 内容: $containerXml")
        throw IllegalStateException("container.xml 中未找到 full-path 属性")
    }

    val path = match.groupValues[1]
    Log.d(TAG, "从 container.xml 提取路径: $path")
    return path
}

private fun extractFirstChapterFromOpf(opfContent: String, opfPath: String): Pair<String, String> {
    // 获取 OPF 文件所在目录
    val opfBaseDir = if (opfPath.contains("/")) {
        opfPath.substring(0, opfPath.lastIndexOf("/"))
    } else {
        ""
    }
    Log.d(TAG, "OPF 基础目录: $opfBaseDir")

    // 1. 解析 manifest
    val manifestRegex = """<manifest[^>]*>([\s\S]*?)</manifest>""".toRegex()
    val manifestMatch = manifestRegex.find(opfContent)

    if (manifestMatch == null) {
        throw IllegalStateException("OPF 文件中未找到 manifest")
    }

    val manifestSection = manifestMatch.groupValues[1]
    Log.d(TAG, "manifest 部分大小: ${manifestSection.length}")

    // 2. 解析 spine 获取第一个 itemref
    val spineRegex = """<spine[^>]*>([\s\S]*?)</spine>""".toRegex()
    val spineMatch = spineRegex.find(opfContent)

    if (spineMatch == null) {
        throw IllegalStateException("OPF 文件中未找到 spine")
    }

    val spineSection = spineMatch.groupValues[1]
    Log.d(TAG, "spine 部分大小: ${spineSection.length}")

    // 3. 获取第一个 itemref 的 idref
    val itemrefRegex = """<itemref[^>]*idref=["']([^"']+)["'][^>]*>""".toRegex()
    val firstItemref = itemrefRegex.find(spineSection)

    if (firstItemref == null) {
        Log.e(TAG, "spine 内容: $spineSection")
        throw IllegalStateException("spine 中未找到 itemref")
    }

    val idRef = firstItemref.groupValues[1]
    Log.d(TAG, "第一个 spine 的 idRef: $idRef")

    // 4. 在 manifest 中查找对应的 item
    val itemRegex = """<item\s+[^>]*id=["']${Regex.escape(idRef)}["'][^>]*href=["']([^"']+)["'][^>]*>""".toRegex()
    val itemMatch = itemRegex.find(manifestSection)

    if (itemMatch == null) {
        // 尝试更宽松的匹配
        val fallbackRegex = """href=["']([^"']+\.(x?html?|htm))["']""".toRegex()
        val fallbackMatch = fallbackRegex.find(manifestSection)

        if (fallbackMatch != null) {
            val href = fallbackMatch.groupValues[1]
            Log.w(TAG, "使用备用方法找到章节: $href")
            return Pair(href, opfBaseDir)
        }

        throw IllegalStateException("manifest 中未找到 id='$idRef' 的条目")
    }

    val href = itemMatch.groupValues[1]
    Log.d(TAG, "找到对应的 href: $href")

    return Pair(href, opfBaseDir)
}

private fun wrapHtmlWithStyles(
    rawHtml: String,
    baseDir: String,
    zipFile: ZipFile
): String {
    // 提取 body 内容
    val bodyRegex = """<body[^>]*>([\s\S]*?)</body>""".toRegex(RegexOption.IGNORE_CASE)
    val bodyMatch = bodyRegex.find(rawHtml)

    val bodyContent = if (bodyMatch != null) {
        bodyMatch.groupValues[1]
    } else {
        // 如果没有找到 body，使用整个内容
        rawHtml
    }

    // 尝试提取或内联 CSS
    val cssContent = extractCssFromZip(zipFile, baseDir)

    // 构建完整的 HTML
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                    font-size: 18px;
                    line-height: 1.6;
                    color: #333;
                    margin: 20px;
                    padding: 0;
                    word-wrap: break-word;
                    -webkit-text-size-adjust: 100%;
                }
                h1, h2, h3, h4, h5, h6 {
                    color: #222;
                    margin-top: 1.5em;
                    margin-bottom: 0.5em;
                }
                p {
                    margin-bottom: 1.2em;
                    text-align: justify;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                    margin: 20px auto;
                }
                a {
                    color: #0066cc;
                    text-decoration: none;
                }
                a:hover {
                    text-decoration: underline;
                }
                blockquote {
                    border-left: 4px solid #ddd;
                    margin: 20px 0;
                    padding: 10px 20px;
                    background-color: #f9f9f9;
                }
                pre, code {
                    font-family: 'Courier New', monospace;
                    background-color: #f5f5f5;
                    padding: 2px 4px;
                    border-radius: 3px;
                }
                pre {
                    padding: 15px;
                    overflow-x: auto;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 20px 0;
                }
                th, td {
                    border: 1px solid #ddd;
                    padding: 8px 12px;
                    text-align: left;
                }
                th {
                    background-color: #f2f2f2;
                }
                hr {
                    border: none;
                    border-top: 1px solid #ddd;
                    margin: 40px 0;
                }
                $cssContent
            </style>
        </head>
        <body>
            $bodyContent
            <div style="height: 100px;"></div>
        </body>
        </html>
    """.trimIndent()
}

private fun extractCssFromZip(zipFile: ZipFile, baseDir: String): String {
    val cssEntries = zipFile.entries().asSequence()
        .filter { !it.isDirectory && it.name.endsWith(".css") }
        .toList()

    if (cssEntries.isEmpty()) {
        return ""
    }

    val cssContents = mutableListOf<String>()
    for (entry in cssEntries.take(5)) { // 限制最多5个CSS文件
        try {
            val css = zipFile.getInputStream(entry).use { inputStream ->
                inputStream.readBytes().toString(StandardCharsets.UTF_8)
            }
            cssContents.add(css)
            Log.d(TAG, "加载 CSS 文件: ${entry.name}, 大小: ${css.length}")
        } catch (e: Exception) {
            Log.w(TAG, "加载 CSS 文件失败: ${entry.name}", e)
        }
    }

    return cssContents.joinToString("\n\n")
}

// 备用的简化解析方法（如果主方法失败）
private fun loadFirstChapterSimple(epubPath: String): String {
    Log.d(TAG, "尝试简化解析方法")
    val file = File(epubPath)
    ZipFile(file).use { zipFile ->
        // 查找第一个HTML文件
        val htmlEntry = zipFile.entries().asSequence()
            .filter { !it.isDirectory }
            .firstOrNull {
                it.name.endsWith(".html") ||
                        it.name.endsWith(".xhtml") ||
                        it.name.endsWith(".htm")
            }

        if (htmlEntry == null) {
            throw IllegalStateException("EPUB 中未找到 HTML 文件")
        }

        Log.d(TAG, "找到 HTML 文件: ${htmlEntry.name}")
        val content = zipFile.getInputStream(htmlEntry).use { inputStream ->
            inputStream.readBytes().toString(StandardCharsets.UTF_8)
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: sans-serif;
                        padding: 20px;
                        line-height: 1.6;
                    }
                </style>
            </head>
            <body>
                $content
            </body>
            </html>
        """.trimIndent()
    }
}